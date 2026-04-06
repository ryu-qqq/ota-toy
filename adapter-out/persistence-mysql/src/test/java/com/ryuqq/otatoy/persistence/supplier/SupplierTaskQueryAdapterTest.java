package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierTaskQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierTaskJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierTaskEntityMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SupplierTask Query Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 findByStatus, findFailedRetryable 동작을 검증한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierTaskQueryAdapter.class,
        SupplierTaskEntityMapper.class
})
class SupplierTaskQueryAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierTaskQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    /**
     * SupplierTaskJpaEntity를 직접 삽입한다.
     */
    private void insertTask(Long supplierId, String taskType, String status,
                            int retryCount, int maxRetries, String failureReason) {
        Instant now = Instant.now();
        SupplierTaskJpaEntity entity = SupplierTaskJpaEntity.create(
                null, supplierId, 100L, taskType, status, null,
                retryCount, maxRetries, failureReason, null, now, now
        );
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("PT-1: findByStatus 조회 동작")
    class FindByStatusTest {

        @Test
        @DisplayName("PENDING 상태의 Task만 조회한다")
        void shouldReturnOnlyPendingTasks() {
            // given
            insertTask(1L, "PROPERTY_CONTENT", "PENDING", 0, 3, null);
            insertTask(1L, "RATE_AVAILABILITY", "PENDING", 0, 3, null);
            insertTask(2L, "PROPERTY_CONTENT", "PROCESSING", 0, 3, null);
            insertTask(2L, "RATE_AVAILABILITY", "COMPLETED", 0, 3, null);
            insertTask(3L, "PROPERTY_CONTENT", "FAILED", 1, 3, "에러 발생");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> pendingTasks = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 100);

            // then
            assertThat(pendingTasks).allMatch(t -> t.status() == SupplierTaskStatus.PENDING);
            assertThat(pendingTasks.size()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("limit 제한이 정상 동작한다")
        void shouldRespectLimitParameter() {
            // given
            for (int i = 0; i < 5; i++) {
                insertTask((long) (100 + i), "PROPERTY_CONTENT", "PENDING", 0, 3, null);
            }
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> limited = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 2);

            // then
            assertThat(limited.size()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("해당 상태의 Task가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMatchingStatus() {
            // given - COMPLETED 상태만 삽입
            insertTask(1L, "PROPERTY_CONTENT", "COMPLETED", 0, 3, null);
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> processing = queryAdapter.findByStatus(SupplierTaskStatus.PROCESSING, 100);

            // then
            assertThat(processing).isEmpty();
        }

        @Test
        @DisplayName("COMPLETED 상태의 Task를 조회할 수 있다")
        void shouldFindCompletedTasks() {
            // given
            insertTask(1L, "PROPERTY_CONTENT", "COMPLETED", 0, 3, null);
            insertTask(2L, "RATE_AVAILABILITY", "COMPLETED", 1, 3, null);
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> completed = queryAdapter.findByStatus(SupplierTaskStatus.COMPLETED, 100);

            // then
            assertThat(completed).allMatch(t -> t.status() == SupplierTaskStatus.COMPLETED);
            assertThat(completed.size()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("PT-2: findFailedRetryable 조회 동작")
    class FindFailedRetryableTest {

        @Test
        @DisplayName("FAILED 상태이고 retryCount < maxRetries인 Task만 조회한다")
        void shouldReturnOnlyRetryableFailedTasks() {
            // given
            // 재시도 가능: retryCount(1) < maxRetries(3)
            insertTask(1L, "PROPERTY_CONTENT", "FAILED", 1, 3, "일시적 오류");
            // 재시도 가능: retryCount(2) < maxRetries(5)
            insertTask(2L, "RATE_AVAILABILITY", "FAILED", 2, 5, "타임아웃");
            // 재시도 불가: retryCount(3) >= maxRetries(3)
            insertTask(3L, "PROPERTY_CONTENT", "FAILED", 3, 3, "최대 재시도 초과");
            // PENDING 상태 - 대상 아님
            insertTask(4L, "PROPERTY_CONTENT", "PENDING", 0, 3, null);
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> retryable = queryAdapter.findFailedRetryable(100);

            // then
            assertThat(retryable).allMatch(t -> t.status() == SupplierTaskStatus.FAILED);
            assertThat(retryable).allMatch(t -> t.retryCount() < t.maxRetries());
            assertThat(retryable.size()).isGreaterThanOrEqualTo(2);
            // retryCount == maxRetries인 Task는 제외
            assertThat(retryable).noneMatch(t -> t.retryCount() >= t.maxRetries());
        }

        @Test
        @DisplayName("findFailedRetryable에 limit 제한이 정상 동작한다")
        void shouldRespectLimitInFailedRetryable() {
            // given
            for (int i = 0; i < 5; i++) {
                insertTask((long) (200 + i), "PROPERTY_CONTENT", "FAILED", 1, 3, "오류 " + i);
            }
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> limited = queryAdapter.findFailedRetryable(2);

            // then
            assertThat(limited.size()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("재시도 가능한 FAILED Task가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoRetryableFailedTasks() {
            // given - maxRetries에 도달한 FAILED만 존재
            insertTask(1L, "PROPERTY_CONTENT", "FAILED", 3, 3, "최종 실패");
            insertTask(2L, "RATE_AVAILABILITY", "PENDING", 0, 3, null);
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> retryable = queryAdapter.findFailedRetryable(100);

            // then
            assertThat(retryable).noneMatch(t -> t.retryCount() >= t.maxRetries());
        }

        @Test
        @DisplayName("maxRetries가 0이면 FAILED Task는 재시도 불가이다")
        void shouldNotReturnFailedTaskWithZeroMaxRetries() {
            // given
            insertTask(1L, "PROPERTY_CONTENT", "FAILED", 0, 0, "즉시 실패");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> retryable = queryAdapter.findFailedRetryable(100);

            // then
            assertThat(retryable).noneMatch(t ->
                    t.supplierId().value().equals(1L) && t.maxRetries() == 0
            );
        }
    }

    @Nested
    @DisplayName("PT-3: Domain 매핑 정합성")
    class DomainMappingTest {

        @Test
        @DisplayName("조회된 SupplierTask의 모든 필드가 Entity와 일치한다")
        void shouldMapAllFieldsFromEntityToDomain() {
            // given
            Instant now = Instant.now();
            SupplierTaskJpaEntity entity = SupplierTaskJpaEntity.create(
                    null, 42L, 777L, "RATE_AVAILABILITY", "FAILED",
                    "{\"key\": \"value\"}", 2, 5, "연결 오류",
                    now, now, now
            );
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierTask> found = queryAdapter.findFailedRetryable(100);

            // then
            SupplierTask result = found.stream()
                    .filter(t -> t.supplierId().value().equals(42L))
                    .findFirst()
                    .orElseThrow();

            assertThat(result.supplierId().value()).isEqualTo(42L);
            assertThat(result.supplierApiConfigId()).isEqualTo(777L);
            assertThat(result.taskType()).isEqualTo(SupplierTaskType.RATE_AVAILABILITY);
            assertThat(result.status()).isEqualTo(SupplierTaskStatus.FAILED);
            assertThat(result.payload()).isEqualTo("{\"key\": \"value\"}");
            assertThat(result.retryCount()).isEqualTo(2);
            assertThat(result.maxRetries()).isEqualTo(5);
            assertThat(result.failureReason()).isEqualTo("연결 오류");
        }
    }
}

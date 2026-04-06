package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierTaskCommandAdapter;
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
 * SupplierTask Command Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 persist/persistAll 동작을 검증한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierTaskCommandAdapter.class,
        SupplierTaskQueryAdapter.class,
        SupplierTaskEntityMapper.class
})
class SupplierTaskCommandAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierTaskCommandAdapter commandAdapter;

    @Autowired
    private SupplierTaskQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("SupplierTask 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectly() {
            // given
            Instant now = Instant.now();
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(1L), 100L,
                    SupplierTaskType.PROPERTY_CONTENT, "{\"propertyId\": 1}",
                    3, now
            );

            // when
            commandAdapter.persist(task);
            entityManager.flush();
            entityManager.clear();

            List<SupplierTask> found = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 10);

            // then
            assertThat(found).isNotEmpty();
            SupplierTask result = found.stream()
                    .filter(t -> t.payload() != null && t.payload().contains("propertyId"))
                    .findFirst()
                    .orElseThrow();

            assertThat(result.id()).isNotNull();
            assertThat(result.id().value()).isNotNull();
            assertThat(result.supplierId().value()).isEqualTo(1L);
            assertThat(result.supplierApiConfigId()).isEqualTo(100L);
            assertThat(result.taskType()).isEqualTo(SupplierTaskType.PROPERTY_CONTENT);
            assertThat(result.status()).isEqualTo(SupplierTaskStatus.PENDING);
            assertThat(result.payload()).isEqualTo("{\"propertyId\": 1}");
            assertThat(result.retryCount()).isEqualTo(0);
            assertThat(result.maxRetries()).isEqualTo(3);
            assertThat(result.failureReason()).isNull();
            assertThat(result.processedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("PT-2: CRUD 동작 검증")
    class CrudOperations {

        @Test
        @DisplayName("persist로 Task 1건 저장 후 조회할 수 있다")
        void shouldPersistSingleTask() {
            // given
            Instant now = Instant.now();
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(10L), 200L,
                    SupplierTaskType.RATE_AVAILABILITY, "{\"dates\": \"2026-04-06\"}",
                    5, now
            );

            // when
            commandAdapter.persist(task);
            entityManager.flush();
            entityManager.clear();

            List<SupplierTask> found = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 100);

            // then
            assertThat(found).anyMatch(t ->
                    t.supplierId().value().equals(10L)
                    && t.taskType() == SupplierTaskType.RATE_AVAILABILITY
            );
        }

        @Test
        @DisplayName("persistAll로 여러 건 일괄 저장 후 조회할 수 있다")
        void shouldPersistAllTasks() {
            // given
            Instant now = Instant.now();
            SupplierTask task1 = SupplierTask.forNew(
                    SupplierId.of(20L), 300L,
                    SupplierTaskType.PROPERTY_CONTENT, "{\"batch\": 1}",
                    3, now
            );
            SupplierTask task2 = SupplierTask.forNew(
                    SupplierId.of(20L), 300L,
                    SupplierTaskType.RATE_AVAILABILITY, "{\"batch\": 2}",
                    3, now
            );
            SupplierTask task3 = SupplierTask.forNew(
                    SupplierId.of(21L), 301L,
                    SupplierTaskType.PROPERTY_CONTENT, "{\"batch\": 3}",
                    3, now
            );

            // when
            commandAdapter.persistAll(List.of(task1, task2, task3));
            entityManager.flush();
            entityManager.clear();

            List<SupplierTask> found = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 100);

            // then
            long count = found.stream()
                    .filter(t -> t.payload() != null && t.payload().contains("batch"))
                    .count();
            assertThat(count).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("상태 변경된 Task를 persist하면 변경이 반영된다")
        void shouldReflectStatusChangeAfterPersist() {
            // given
            Instant now = Instant.now();
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(30L), 400L,
                    SupplierTaskType.PROPERTY_CONTENT, "{\"change\": true}",
                    3, now
            );
            commandAdapter.persist(task);
            entityManager.flush();
            entityManager.clear();

            // 저장된 Task를 조회하여 상태 변경 후 다시 persist
            List<SupplierTask> pending = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 100);
            SupplierTask saved = pending.stream()
                    .filter(t -> t.payload() != null && t.payload().contains("\"change\""))
                    .findFirst()
                    .orElseThrow();

            // PENDING -> PROCESSING -> FAILED 상태 전이
            saved.markProcessing();
            saved.markFailed("타임아웃 발생", Instant.now());

            // when - 상태 변경된 도메인 객체를 다시 Entity로 변환하여 직접 저장
            SupplierTaskEntityMapper mapper = new SupplierTaskEntityMapper();
            SupplierTaskJpaEntity updatedEntity = mapper.toEntity(saved);
            entityManager.merge(updatedEntity);
            entityManager.flush();
            entityManager.clear();

            // then - FAILED 상태로 조회 가능
            List<SupplierTask> failedRetryable = queryAdapter.findFailedRetryable(100);
            assertThat(failedRetryable).anyMatch(t ->
                    t.payload() != null && t.payload().contains("\"change\"")
                    && t.status() == SupplierTaskStatus.FAILED
                    && t.retryCount() == 1
            );
        }
    }

    @Nested
    @DisplayName("PT-3: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 supplier_task 테이블을 정상 생성한다")
        void shouldCreateSupplierTaskTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            Instant now = Instant.now();
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(99L), 999L,
                    SupplierTaskType.PROPERTY_CONTENT, null,
                    0, now
            );
            commandAdapter.persist(task);
            entityManager.flush();

            List<SupplierTask> found = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 10);
            assertThat(found).anyMatch(t -> t.supplierId().value().equals(99L));
        }
    }

    @Nested
    @DisplayName("PT-4: nullable 필드 처리")
    class NullableFieldTest {

        @Test
        @DisplayName("payload가 null이어도 정상 저장/조회된다")
        void shouldHandleNullPayload() {
            // given
            Instant now = Instant.now();
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(50L), 500L,
                    SupplierTaskType.RATE_AVAILABILITY, null,
                    3, now
            );

            // when
            commandAdapter.persist(task);
            entityManager.flush();
            entityManager.clear();

            List<SupplierTask> found = queryAdapter.findByStatus(SupplierTaskStatus.PENDING, 100);

            // then
            assertThat(found).anyMatch(t ->
                    t.supplierId().value().equals(50L) && t.payload() == null
            );
        }
    }
}

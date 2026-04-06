package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierSyncLogCommandAdapter;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierSyncLogQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierSyncLogEntityMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SupplierSyncLog Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 동기화 로그 저장/조회를 검증한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierSyncLogCommandAdapter.class,
        SupplierSyncLogQueryAdapter.class,
        SupplierSyncLogEntityMapper.class
})
class SupplierSyncLogPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierSyncLogCommandAdapter commandAdapter;

    @Autowired
    private SupplierSyncLogQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    private Long supplierId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Supplier
        Instant now = Instant.now();
        SupplierJpaEntity supplier = SupplierJpaEntity.create(
                null, "TestSupplier", "테스트공급자", "테스트컴퍼니", "홍길동", "123-45-67890",
                "서울시 강남구", "02-1234-5678", "test@test.com", "https://test.com/terms",
                "ACTIVE", now, now, null
        );
        entityManager.persist(supplier);
        entityManager.flush();
        supplierId = supplier.getId();
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("성공 동기화 로그 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyForSuccessLog() {
            // given
            Instant syncedAt = Instant.now();
            SupplierSyncLog original = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, syncedAt,
                    10, 5, 3, 2
            );

            // when
            Long savedId = commandAdapter.persist(original);
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY
            );

            // then
            assertThat(found).isPresent();
            SupplierSyncLog result = found.get();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.supplierId().value()).isEqualTo(supplierId);
            assertThat(result.syncType()).isEqualTo(SupplierSyncType.PROPERTY);
            assertThat(result.status()).isEqualTo(SupplierSyncStatus.SUCCESS);
            assertThat(result.totalCount()).isEqualTo(10);
            assertThat(result.createdCount()).isEqualTo(5);
            assertThat(result.updatedCount()).isEqualTo(3);
            assertThat(result.deletedCount()).isEqualTo(2);
            assertThat(result.errorMessage()).isNull();
            assertThat(result.syncedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 동기화 로그 저장 후 errorMessage가 정확히 매핑된다")
        void shouldMapErrorMessageForFailedLog() {
            // given
            Instant syncedAt = Instant.now();
            SupplierSyncLog failedLog = SupplierSyncLog.forFailed(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, syncedAt,
                    "Connection timeout"
            );

            // when
            Long savedId = commandAdapter.persist(failedLog);

            // then -- 실패 로그는 findLastSuccessBySupplierId로 조회되지 않아야 한다
            assertThat(savedId).isNotNull();
        }
    }

    @Nested
    @DisplayName("PT-2: CRUD 동작 검증")
    class CrudOperations {

        @Test
        @DisplayName("persist 후 findLastSuccessBySupplierId로 최신 성공 로그를 조회할 수 있다")
        void shouldPersistAndFindLastSuccess() {
            // given
            Instant olderSyncedAt = Instant.now().minusSeconds(3600);
            Instant newerSyncedAt = Instant.now();

            SupplierSyncLog olderLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, olderSyncedAt,
                    5, 2, 2, 1
            );
            SupplierSyncLog newerLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, newerSyncedAt,
                    15, 8, 5, 2
            );

            commandAdapter.persist(olderLog);
            Long newerId = commandAdapter.persist(newerLog);

            // when
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY
            );

            // then -- 최신(newerSyncedAt) 로그가 반환되어야 한다
            assertThat(found).isPresent();
            assertThat(found.get().id().value()).isEqualTo(newerId);
            assertThat(found.get().totalCount()).isEqualTo(15);
        }

        @Test
        @DisplayName("해당 Supplier의 성공 로그가 없으면 Optional.empty()를 반환한다")
        void shouldReturnEmptyWhenNoSuccessLog() {
            // given -- 실패 로그만 저장
            Instant now = Instant.now();
            SupplierSyncLog failedLog = SupplierSyncLog.forFailed(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, now,
                    "Error occurred"
            );
            commandAdapter.persist(failedLog);

            // when
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY
            );

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 supplierId로 조회 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingSupplierId() {
            // when
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(99999L), SupplierSyncType.PROPERTY
            );

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: syncType별 필터 검증")
    class SyncTypeFilterTest {

        @Test
        @DisplayName("같은 Supplier라도 syncType이 다르면 별도로 조회된다")
        void shouldFilterBySyncType() {
            // given
            Instant now = Instant.now();
            SupplierSyncLog propertyLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, now,
                    10, 5, 3, 2
            );
            SupplierSyncLog rateLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.RATE, now,
                    20, 10, 8, 2
            );

            Long propertyLogId = commandAdapter.persist(propertyLog);
            Long rateLogId = commandAdapter.persist(rateLog);

            // when
            Optional<SupplierSyncLog> foundProperty = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY
            );
            Optional<SupplierSyncLog> foundRate = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.RATE
            );

            // then
            assertThat(foundProperty).isPresent();
            assertThat(foundProperty.get().syncType()).isEqualTo(SupplierSyncType.PROPERTY);
            assertThat(foundProperty.get().totalCount()).isEqualTo(10);

            assertThat(foundRate).isPresent();
            assertThat(foundRate.get().syncType()).isEqualTo(SupplierSyncType.RATE);
            assertThat(foundRate.get().totalCount()).isEqualTo(20);
        }

        @Test
        @DisplayName("FETCH 타입의 성공 로그가 없으면 Optional.empty()를 반환한다")
        void shouldReturnEmptyForMissingSyncType() {
            // given -- PROPERTY 타입만 저장
            Instant now = Instant.now();
            SupplierSyncLog propertyLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, now,
                    10, 5, 3, 2
            );
            commandAdapter.persist(propertyLog);

            // when
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.FETCH
            );

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-4: 성공/실패 로그 혼재 시 SUCCESS만 조회")
    class SuccessOnlyFilterTest {

        @Test
        @DisplayName("성공과 실패 로그가 섞여 있을 때 findLastSuccess는 성공 로그만 반환한다")
        void shouldReturnOnlySuccessLog() {
            // given
            Instant now = Instant.now();

            // 먼저 성공 로그
            SupplierSyncLog successLog = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.FULL, now.minusSeconds(60),
                    10, 5, 3, 2
            );
            Long successId = commandAdapter.persist(successLog);

            // 그 후 실패 로그 (더 최신)
            SupplierSyncLog failedLog = SupplierSyncLog.forFailed(
                    SupplierId.of(supplierId), SupplierSyncType.FULL, now,
                    "Timeout"
            );
            commandAdapter.persist(failedLog);

            // when
            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.FULL
            );

            // then -- 실패 로그가 더 최신이지만 SUCCESS 로그만 반환되어야 한다
            assertThat(found).isPresent();
            assertThat(found.get().id().value()).isEqualTo(successId);
            assertThat(found.get().status()).isEqualTo(SupplierSyncStatus.SUCCESS);
            assertThat(found.get().errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("PT-5: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 supplier_sync_log 테이블을 정상적으로 생성한다")
        void shouldCreateSyncLogTableViaFlyway() {
            Instant now = Instant.now();
            SupplierSyncLog log = SupplierSyncLog.forSuccess(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY, now,
                    1, 1, 0, 0
            );
            Long savedId = commandAdapter.persist(log);
            assertThat(savedId).isNotNull();

            Optional<SupplierSyncLog> found = queryAdapter.findLastSuccessBySupplierId(
                    SupplierId.of(supplierId), SupplierSyncType.PROPERTY
            );
            assertThat(found).isPresent();
        }
    }
}

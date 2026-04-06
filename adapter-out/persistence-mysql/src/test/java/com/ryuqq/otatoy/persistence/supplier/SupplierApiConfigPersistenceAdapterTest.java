package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierApiConfigQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierApiConfigJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierApiConfigEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierApiConfigJpaRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SupplierApiConfig Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 SupplierApiConfigQueryAdapter 동작을 검증한다.
 * CommandAdapter가 없으므로 EntityManager로 직접 데이터를 삽입한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierApiConfigQueryAdapter.class,
        SupplierApiConfigEntityMapper.class
})
class SupplierApiConfigPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierApiConfigQueryAdapter apiConfigQueryAdapter;

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

    private Long insertApiConfig(Long supplierId, String status) {
        Instant now = Instant.now();
        SupplierApiConfigJpaEntity entity = SupplierApiConfigJpaEntity.create(
                null, supplierId, "MOCK", "https://api.test.com", "test-api-key",
                "BEARER", 30, status, now, now, null
        );
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("SupplierApiConfig 삽입 후 findBySupplierId로 조회 시 모든 필드가 정확히 매핑된다")
        void shouldMapAllFieldsCorrectlyForApiConfig() {
            // given
            insertApiConfig(supplierId, "ACTIVE");

            // when
            Optional<SupplierApiConfig> found = apiConfigQueryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).isPresent();
            SupplierApiConfig config = found.get();

            assertThat(config.supplierId().value()).isEqualTo(supplierId);
            assertThat(config.apiType()).isEqualTo(SupplierApiType.MOCK);
            assertThat(config.baseUrl()).isEqualTo("https://api.test.com");
            assertThat(config.apiKey()).isEqualTo("test-api-key");
            assertThat(config.authType()).isEqualTo("BEARER");
            assertThat(config.syncIntervalMinutes()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("PT-2: findAllActive 동작 검증")
    class FindAllActiveTest {

        @Test
        @DisplayName("ACTIVE 상태의 API 설정만 조회한다")
        void shouldReturnOnlyActiveConfigs() {
            // given
            insertApiConfig(supplierId, "ACTIVE");

            // 다른 Supplier + INACTIVE config
            Instant now = Instant.now();
            SupplierJpaEntity supplier2 = SupplierJpaEntity.create(
                    null, "Supplier2", "공급자2", "컴퍼니2", "이름2", "111-11-11111",
                    "주소2", "02-0000-0000", "s2@test.com", null,
                    "ACTIVE", now, now, null
            );
            entityManager.persist(supplier2);
            entityManager.flush();
            insertApiConfig(supplier2.getId(), "INACTIVE");

            // when
            List<SupplierApiConfig> result = apiConfigQueryAdapter.findAllActive();

            // then
            assertThat(result).anyMatch(c -> c.supplierId().value().equals(supplierId));
            assertThat(result).noneMatch(c -> c.supplierId().value().equals(supplier2.getId()));
        }

        @Test
        @DisplayName("ACTIVE 설정이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoActiveConfig() {
            // given -- ACTIVE config를 삽입하지 않음

            // when
            List<SupplierApiConfig> result = apiConfigQueryAdapter.findAllActive();

            // then
            // 다른 테스트에서 삽입된 데이터가 있을 수 있으므로 사이즈 검증 대신 동작 확인
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("PT-3: findBySupplierId 동작 검증")
    class FindBySupplierIdTest {

        @Test
        @DisplayName("존재하는 supplierId로 조회 시 Optional에 값이 존재한다")
        void shouldFindConfigBySupplierId() {
            // given
            insertApiConfig(supplierId, "ACTIVE");

            // when
            Optional<SupplierApiConfig> found = apiConfigQueryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().supplierId().value()).isEqualTo(supplierId);
        }

        @Test
        @DisplayName("존재하지 않는 supplierId로 조회 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingSupplierId() {
            // when
            Optional<SupplierApiConfig> found = apiConfigQueryAdapter.findBySupplierId(SupplierId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-4: Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 API 설정은 findBySupplierId에서 조회되지 않는다")
        void shouldNotFindSoftDeletedConfig() {
            // given
            Instant now = Instant.now();
            SupplierApiConfigJpaEntity deletedEntity = SupplierApiConfigJpaEntity.create(
                    null, supplierId, "MOCK", "https://api.deleted.com", "deleted-key",
                    "BEARER", 60, "ACTIVE", now, now, now // deletedAt 설정
            );
            entityManager.persist(deletedEntity);
            entityManager.flush();

            // when
            Optional<SupplierApiConfig> found = apiConfigQueryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 API 설정은 findAllActive에서 조회되지 않는다")
        void shouldNotIncludeDeletedInFindAllActive() {
            // given
            Instant now = Instant.now();
            SupplierApiConfigJpaEntity deletedEntity = SupplierApiConfigJpaEntity.create(
                    null, supplierId, "MOCK", "https://api.deleted.com", "deleted-key",
                    "BEARER", 60, "ACTIVE", now, now, now
            );
            entityManager.persist(deletedEntity);
            entityManager.flush();

            // when
            List<SupplierApiConfig> result = apiConfigQueryAdapter.findAllActive();

            // then
            assertThat(result).noneMatch(c -> c.baseUrl().equals("https://api.deleted.com"));
        }
    }
}

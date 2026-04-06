package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.Supplier;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierStatus;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierJpaRepository;
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
 * Supplier Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 SupplierQueryAdapter 동작을 검증한다.
 * Supplier는 CommandAdapter가 없으므로 EntityManager로 직접 데이터를 삽입한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierQueryAdapter.class,
        SupplierEntityMapper.class
})
class SupplierPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierQueryAdapter supplierQueryAdapter;

    @Autowired
    private EntityManager entityManager;

    /**
     * Supplier Entity를 직접 삽입한다. CommandAdapter가 없으므로 EntityManager 사용.
     */
    private Long insertSupplier(String name, String nameKr, String status) {
        Instant now = Instant.now();
        SupplierJpaEntity entity = SupplierJpaEntity.create(
                null, name, nameKr, "테스트컴퍼니", "홍길동", "123-45-67890",
                "서울시 강남구", "02-1234-5678", "test@test.com", "https://test.com/terms",
                status, now, now, null
        );
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    private Long insertDeletedSupplier(String name) {
        Instant now = Instant.now();
        SupplierJpaEntity entity = SupplierJpaEntity.create(
                null, name, "삭제공급자", "삭제컴퍼니", "삭제자", "000-00-00000",
                "삭제주소", "00-0000-0000", "deleted@test.com", null,
                "ACTIVE", now, now, now
        );
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("ACTIVE 상태 Supplier 삽입 후 findByStatus로 조회 시 모든 필드가 정확히 매핑된다")
        void shouldMapAllFieldsCorrectlyForActiveSupplier() {
            // given
            Instant now = Instant.now();
            SupplierJpaEntity entity = SupplierJpaEntity.create(
                    null, "TestSupplier", "테스트공급자", "테스트컴퍼니", "홍길동", "123-45-67890",
                    "서울시 강남구 테헤란로 123", "02-1234-5678", "supplier@test.com",
                    "https://test.com/terms", "ACTIVE", now, now, null
            );
            entityManager.persist(entity);
            entityManager.flush();

            // when
            List<Supplier> result = supplierQueryAdapter.findByStatus(SupplierStatus.ACTIVE);

            // then
            assertThat(result).isNotEmpty();
            Supplier found = result.stream()
                    .filter(s -> s.name().value().equals("TestSupplier"))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.name().value()).isEqualTo("TestSupplier");
            assertThat(found.nameKr().value()).isEqualTo("테스트공급자");
            assertThat(found.companyTitle().value()).isEqualTo("테스트컴퍼니");
            assertThat(found.ownerName().value()).isEqualTo("홍길동");
            assertThat(found.businessNo().value()).isEqualTo("123-45-67890");
            assertThat(found.address()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(found.phone().value()).isEqualTo("02-1234-5678");
            assertThat(found.email().value()).isEqualTo("supplier@test.com");
            assertThat(found.termsUrl()).isEqualTo("https://test.com/terms");
            assertThat(found.status()).isEqualTo(SupplierStatus.ACTIVE);
            assertThat(found.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("PT-2: findByStatus 조회 동작")
    class FindByStatusTest {

        @Test
        @DisplayName("ACTIVE 상태의 Supplier만 조회한다")
        void shouldReturnOnlyActiveSuppliers() {
            // given
            insertSupplier("ActiveSupplier1", "활성공급자1", "ACTIVE");
            insertSupplier("ActiveSupplier2", "활성공급자2", "ACTIVE");
            insertSupplier("SuspendedSupplier", "정지공급자", "SUSPENDED");

            // when
            List<Supplier> activeList = supplierQueryAdapter.findByStatus(SupplierStatus.ACTIVE);
            List<Supplier> suspendedList = supplierQueryAdapter.findByStatus(SupplierStatus.SUSPENDED);

            // then
            assertThat(activeList).anyMatch(s -> s.name().value().equals("ActiveSupplier1"));
            assertThat(activeList).anyMatch(s -> s.name().value().equals("ActiveSupplier2"));
            assertThat(activeList).noneMatch(s -> s.name().value().equals("SuspendedSupplier"));

            assertThat(suspendedList).anyMatch(s -> s.name().value().equals("SuspendedSupplier"));
            assertThat(suspendedList).noneMatch(s -> s.name().value().equals("ActiveSupplier1"));
        }

        @Test
        @DisplayName("해당 상태의 Supplier가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMatchingStatus() {
            // when
            List<Supplier> result = supplierQueryAdapter.findByStatus(SupplierStatus.TERMINATED);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 Supplier는 findByStatus에서 조회되지 않는다")
        void shouldNotFindSoftDeletedSupplier() {
            // given
            insertDeletedSupplier("DeletedActiveSupplier");
            insertSupplier("NormalActive", "정상공급자", "ACTIVE");

            // when
            List<Supplier> result = supplierQueryAdapter.findByStatus(SupplierStatus.ACTIVE);

            // then
            assertThat(result).noneMatch(s -> s.name().value().equals("DeletedActiveSupplier"));
            assertThat(result).anyMatch(s -> s.name().value().equals("NormalActive"));
        }
    }

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 supplier 테이블을 정상적으로 생성한다")
        void shouldCreateSupplierTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            Long id = insertSupplier("FlywayTest", "플라이웨이테스트", "ACTIVE");
            assertThat(id).isNotNull();

            List<Supplier> found = supplierQueryAdapter.findByStatus(SupplierStatus.ACTIVE);
            assertThat(found).anyMatch(s -> s.name().value().equals("FlywayTest"));
        }
    }

    @Nested
    @DisplayName("PT-5: nullable 필드 처리")
    class NullableFieldTest {

        @Test
        @DisplayName("phone, email, termsUrl이 null이어도 정상 저장/조회된다")
        void shouldHandleNullOptionalFields() {
            // given
            Instant now = Instant.now();
            SupplierJpaEntity entity = SupplierJpaEntity.create(
                    null, "NullFieldSupplier", "널필드공급자", "널컴퍼니", "널이름", "999-99-99999",
                    null, null, null, null,
                    "ACTIVE", now, now, null
            );
            entityManager.persist(entity);
            entityManager.flush();

            // when
            List<Supplier> result = supplierQueryAdapter.findByStatus(SupplierStatus.ACTIVE);

            // then
            Supplier found = result.stream()
                    .filter(s -> s.name().value().equals("NullFieldSupplier"))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.address()).isNull();
            assertThat(found.phone()).isNull();
            assertThat(found.email()).isNull();
            assertThat(found.termsUrl()).isNull();
        }
    }
}

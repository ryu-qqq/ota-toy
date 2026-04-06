package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.supplier.SupplierMappingStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierPropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierPropertyQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierPropertyJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierPropertyEntityMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SupplierProperty Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 Command/Query Adapter 동작을 검증한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierPropertyCommandAdapter.class,
        SupplierPropertyQueryAdapter.class,
        SupplierPropertyEntityMapper.class
})
class SupplierPropertyPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierPropertyCommandAdapter commandAdapter;

    @Autowired
    private SupplierPropertyQueryAdapter queryAdapter;

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
        @DisplayName("SupplierProperty 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectly() {
            // given
            Instant now = Instant.now();
            SupplierProperty original = SupplierProperty.forNew(
                    SupplierId.of(supplierId), PropertyId.of(100L), "EXT-PROP-001", now
            );

            // when
            Long savedId = commandAdapter.persist(original);
            List<SupplierProperty> found = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).isNotEmpty();
            SupplierProperty result = found.stream()
                    .filter(sp -> sp.supplierPropertyCode().equals("EXT-PROP-001"))
                    .findFirst()
                    .orElseThrow();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.supplierId().value()).isEqualTo(supplierId);
            assertThat(result.propertyId().value()).isEqualTo(100L);
            assertThat(result.supplierPropertyCode()).isEqualTo("EXT-PROP-001");
            assertThat(result.status()).isEqualTo(SupplierMappingStatus.MAPPED);
            // forNew는 lastSyncedAt=null
            assertThat(result.lastSyncedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("PT-2: CRUD 동작 검증")
    class CrudOperations {

        @Test
        @DisplayName("persist 후 findBySupplierId로 조회할 수 있다")
        void shouldPersistAndFindBySupplierId() {
            // given
            Instant now = Instant.now();
            SupplierProperty sp1 = SupplierProperty.forNew(
                    SupplierId.of(supplierId), PropertyId.of(100L), "EXT-001", now
            );
            SupplierProperty sp2 = SupplierProperty.forNew(
                    SupplierId.of(supplierId), PropertyId.of(200L), "EXT-002", now
            );

            // when
            commandAdapter.persist(sp1);
            commandAdapter.persist(sp2);
            List<SupplierProperty> found = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).hasSizeGreaterThanOrEqualTo(2);
            assertThat(found).anyMatch(sp -> sp.supplierPropertyCode().equals("EXT-001"));
            assertThat(found).anyMatch(sp -> sp.supplierPropertyCode().equals("EXT-002"));
        }

        @Test
        @DisplayName("다른 supplierId의 데이터는 조회되지 않는다")
        void shouldNotReturnOtherSupplierProperties() {
            // given -- 다른 Supplier 생성
            Instant now = Instant.now();
            SupplierJpaEntity otherSupplier = SupplierJpaEntity.create(
                    null, "OtherSupplier", "다른공급자", "다른컴퍼니", "이름2", "999-99-99999",
                    "주소2", "02-0000-0000", "other@test.com", null,
                    "ACTIVE", now, now, null
            );
            entityManager.persist(otherSupplier);
            entityManager.flush();
            Long otherSupplierId = otherSupplier.getId();

            SupplierProperty myProp = SupplierProperty.forNew(
                    SupplierId.of(supplierId), PropertyId.of(100L), "MY-001", now
            );
            SupplierProperty otherProp = SupplierProperty.forNew(
                    SupplierId.of(otherSupplierId), PropertyId.of(200L), "OTHER-001", now
            );
            commandAdapter.persist(myProp);
            commandAdapter.persist(otherProp);

            // when
            List<SupplierProperty> myResult = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(myResult).anyMatch(sp -> sp.supplierPropertyCode().equals("MY-001"));
            assertThat(myResult).noneMatch(sp -> sp.supplierPropertyCode().equals("OTHER-001"));
        }

        @Test
        @DisplayName("해당 Supplier의 매핑이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyForSupplierWithNoProperties() {
            // when
            List<SupplierProperty> result = queryAdapter.findBySupplierId(SupplierId.of(99999L));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 SupplierProperty는 findBySupplierId에서 조회되지 않는다")
        void shouldNotFindSoftDeletedSupplierProperty() {
            // given
            Instant now = Instant.now();
            SupplierPropertyJpaEntity deletedEntity = SupplierPropertyJpaEntity.create(
                    null, supplierId, 100L, "DELETED-001", now, "MAPPED",
                    now, now, now // deletedAt 설정
            );
            entityManager.persist(deletedEntity);
            entityManager.flush();

            // when
            List<SupplierProperty> result = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(result).noneMatch(sp -> sp.supplierPropertyCode().equals("DELETED-001"));
        }
    }

    @Nested
    @DisplayName("PT-4: lastSyncedAt 필드 처리")
    class LastSyncedAtTest {

        @Test
        @DisplayName("lastSyncedAt이 설정된 reconstituted 객체가 정상 저장/조회된다")
        void shouldHandleLastSyncedAtCorrectly() {
            // given
            Instant now = Instant.now();
            Instant syncedAt = now.minusSeconds(3600);
            SupplierProperty property = SupplierProperty.reconstitute(
                    null, SupplierId.of(supplierId), PropertyId.of(300L),
                    "SYNCED-001", syncedAt, SupplierMappingStatus.MAPPED, now, now
            );

            // when
            Long savedId = commandAdapter.persist(property);
            List<SupplierProperty> found = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            SupplierProperty result = found.stream()
                    .filter(sp -> sp.supplierPropertyCode().equals("SYNCED-001"))
                    .findFirst()
                    .orElseThrow();

            assertThat(result.lastSyncedAt()).isNotNull();
        }
    }
}

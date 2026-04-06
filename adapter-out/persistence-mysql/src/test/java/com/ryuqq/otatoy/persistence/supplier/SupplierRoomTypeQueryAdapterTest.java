package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierMappingStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierRoomTypeQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierPropertyJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierRoomTypeEntityMapper;
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
 * SupplierRoomType Query Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 findBySupplierId 동작을 검증한다.
 * SupplierRoomTypeJpaEntity에 create() 팩토리가 없으므로 네이티브 SQL로 데이터를 삽입한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierRoomTypeQueryAdapter.class,
        SupplierRoomTypeEntityMapper.class
})
class SupplierRoomTypeQueryAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierRoomTypeQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    private Long supplierId;
    private Long supplierPropertyId1;
    private Long supplierPropertyId2;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        // FK 선행 데이터: Supplier
        SupplierJpaEntity supplier = SupplierJpaEntity.create(
                null, "RoomTypeTestSupplier", "객실테스트공급자", "테스트컴퍼니", "홍길동", "123-45-67890",
                "서울시 강남구", "02-1234-5678", "test@test.com", "https://test.com/terms",
                "ACTIVE", now, now, null
        );
        entityManager.persist(supplier);
        entityManager.flush();
        supplierId = supplier.getId();

        // FK 선행 데이터: SupplierProperty 2건
        SupplierPropertyJpaEntity property1 = SupplierPropertyJpaEntity.create(
                null, supplierId, 100L, "EXT-PROP-001", null, "MAPPED",
                now, now, null
        );
        SupplierPropertyJpaEntity property2 = SupplierPropertyJpaEntity.create(
                null, supplierId, 200L, "EXT-PROP-002", null, "MAPPED",
                now, now, null
        );
        entityManager.persist(property1);
        entityManager.persist(property2);
        entityManager.flush();
        supplierPropertyId1 = property1.getId();
        supplierPropertyId2 = property2.getId();
    }

    /**
     * 네이티브 SQL로 supplier_room_type 데이터를 삽입한다.
     * Entity에 create() 팩토리 메서드가 없으므로 직접 SQL 사용.
     */
    private void insertRoomType(Long supplierPropertyId, Long roomTypeId,
                                 String supplierRoomCode, String status) {
        entityManager.createNativeQuery(
                "INSERT INTO supplier_room_type (supplier_property_id, room_type_id, supplier_room_code, status, created_at, updated_at, deleted) " +
                "VALUES (?, ?, ?, ?, NOW(6), NOW(6), 0)"
        )
                .setParameter(1, supplierPropertyId)
                .setParameter(2, roomTypeId)
                .setParameter(3, supplierRoomCode)
                .setParameter(4, status)
                .executeUpdate();
    }

    private void insertDeletedRoomType(Long supplierPropertyId, Long roomTypeId,
                                        String supplierRoomCode) {
        entityManager.createNativeQuery(
                "INSERT INTO supplier_room_type (supplier_property_id, room_type_id, supplier_room_code, status, created_at, updated_at, deleted, deleted_at) " +
                "VALUES (?, ?, ?, 'MAPPED', NOW(6), NOW(6), 1, NOW(6))"
        )
                .setParameter(1, supplierPropertyId)
                .setParameter(2, roomTypeId)
                .setParameter(3, supplierRoomCode)
                .executeUpdate();
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("SupplierRoomType 조회 시 모든 필드가 정확히 매핑된다")
        void shouldMapAllFieldsCorrectly() {
            // given
            insertRoomType(supplierPropertyId1, 1001L, "EXT-ROOM-001", "MAPPED");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> found = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).hasSize(1);
            SupplierRoomType result = found.get(0);

            assertThat(result.id()).isNotNull();
            assertThat(result.id().value()).isNotNull();
            assertThat(result.supplierPropertyId().value()).isEqualTo(supplierPropertyId1);
            assertThat(result.roomTypeId().value()).isEqualTo(1001L);
            assertThat(result.supplierRoomCode()).isEqualTo("EXT-ROOM-001");
            assertThat(result.status()).isEqualTo(SupplierMappingStatus.MAPPED);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PT-2: findBySupplierId 조회 동작")
    class FindBySupplierIdTest {

        @Test
        @DisplayName("supplierId에 매핑된 모든 RoomType을 반환한다 (여러 Property 포함)")
        void shouldReturnAllRoomTypesForSupplier() {
            // given - property1에 2건, property2에 1건
            insertRoomType(supplierPropertyId1, 1001L, "EXT-ROOM-001", "MAPPED");
            insertRoomType(supplierPropertyId1, 1002L, "EXT-ROOM-002", "MAPPED");
            insertRoomType(supplierPropertyId2, 2001L, "EXT-ROOM-003", "MAPPED");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> found = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(found).hasSize(3);
            assertThat(found).anyMatch(rt -> rt.supplierRoomCode().equals("EXT-ROOM-001"));
            assertThat(found).anyMatch(rt -> rt.supplierRoomCode().equals("EXT-ROOM-002"));
            assertThat(found).anyMatch(rt -> rt.supplierRoomCode().equals("EXT-ROOM-003"));
        }

        @Test
        @DisplayName("다른 Supplier의 RoomType은 조회되지 않는다")
        void shouldNotReturnOtherSupplierRoomTypes() {
            // given - 다른 Supplier 생성
            Instant now = Instant.now();
            SupplierJpaEntity otherSupplier = SupplierJpaEntity.create(
                    null, "OtherSupplier", "다른공급자", "다른컴퍼니", "이름2", "999-99-99999",
                    "주소2", "02-0000-0000", "other@test.com", null,
                    "ACTIVE", now, now, null
            );
            entityManager.persist(otherSupplier);
            entityManager.flush();
            Long otherSupplierId = otherSupplier.getId();

            SupplierPropertyJpaEntity otherProperty = SupplierPropertyJpaEntity.create(
                    null, otherSupplierId, 300L, "OTHER-PROP-001", null, "MAPPED",
                    now, now, null
            );
            entityManager.persist(otherProperty);
            entityManager.flush();
            Long otherPropertyId = otherProperty.getId();

            // 내 Supplier의 RoomType
            insertRoomType(supplierPropertyId1, 1001L, "MY-ROOM-001", "MAPPED");
            // 다른 Supplier의 RoomType
            insertRoomType(otherPropertyId, 3001L, "OTHER-ROOM-001", "MAPPED");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> myResult = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(myResult).anyMatch(rt -> rt.supplierRoomCode().equals("MY-ROOM-001"));
            assertThat(myResult).noneMatch(rt -> rt.supplierRoomCode().equals("OTHER-ROOM-001"));
        }

        @Test
        @DisplayName("해당 Supplier의 Property가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoProperties() {
            // when
            List<SupplierRoomType> result = queryAdapter.findBySupplierId(SupplierId.of(99999L));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Property는 있지만 RoomType이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoRoomTypes() {
            // given - setUp에서 Property는 이미 생성됨, RoomType은 삽입하지 않음
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> result = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 SupplierProperty의 RoomType은 조회되지 않는다")
        void shouldNotFindRoomTypesOfDeletedProperty() {
            // given - soft delete된 Property 생성
            Instant now = Instant.now();
            SupplierPropertyJpaEntity deletedProperty = SupplierPropertyJpaEntity.create(
                    null, supplierId, 400L, "DELETED-PROP-001", null, "MAPPED",
                    now, now, now // deletedAt 설정
            );
            entityManager.persist(deletedProperty);
            entityManager.flush();
            Long deletedPropertyId = deletedProperty.getId();

            // 삭제된 Property의 RoomType
            insertRoomType(deletedPropertyId, 4001L, "DELETED-ROOM-001", "MAPPED");
            // 정상 Property의 RoomType
            insertRoomType(supplierPropertyId1, 1001L, "ACTIVE-ROOM-001", "MAPPED");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> result = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(result).anyMatch(rt -> rt.supplierRoomCode().equals("ACTIVE-ROOM-001"));
            assertThat(result).noneMatch(rt -> rt.supplierRoomCode().equals("DELETED-ROOM-001"));
        }
    }

    @Nested
    @DisplayName("PT-4: UNMAPPED 상태 포함 조회")
    class StatusFilterTest {

        @Test
        @DisplayName("UNMAPPED 상태의 RoomType도 조회된다 (상태 필터링은 Adapter 레벨이 아님)")
        void shouldIncludeUnmappedRoomTypes() {
            // given
            insertRoomType(supplierPropertyId1, 1001L, "MAPPED-ROOM", "MAPPED");
            insertRoomType(supplierPropertyId1, 1002L, "UNMAPPED-ROOM", "UNMAPPED");
            entityManager.flush();
            entityManager.clear();

            // when
            List<SupplierRoomType> result = queryAdapter.findBySupplierId(SupplierId.of(supplierId));

            // then
            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(rt -> rt.status() == SupplierMappingStatus.MAPPED);
            assertThat(result).anyMatch(rt -> rt.status() == SupplierMappingStatus.UNMAPPED);
        }
    }
}

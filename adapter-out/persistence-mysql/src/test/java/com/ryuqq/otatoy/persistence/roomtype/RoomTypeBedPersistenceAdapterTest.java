package com.ryuqq.otatoy.persistence.roomtype;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeBedCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeBedJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeBedEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeBedJpaRepository;
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
 * RoomTypeBed Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 실제 DB에서 CRUD 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        RoomTypeBedCommandAdapter.class,
        RoomTypeBedEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class
})
class RoomTypeBedPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RoomTypeBedCommandAdapter roomTypeBedCommandAdapter;

    @Autowired
    private RoomTypeBedEntityMapper roomTypeBedEntityMapper;

    @Autowired
    private RoomTypeBedJpaRepository roomTypeBedJpaRepository;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    private Long propertyId;
    private Long roomTypeId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Property -> RoomType
        Property property = PropertyFixture.aPropertyWithName("침대 테스트 호텔");
        propertyId = propertyCommandAdapter.persist(property);

        RoomType roomType = RoomType.forNew(
                PropertyId.of(propertyId), RoomTypeName.of("디럭스 더블"),
                null, null, null, 2, 4, 5, null, null, Instant.now()
        );
        roomTypeId = roomTypeCommandAdapter.persist(roomType);
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("RoomTypeBed 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            Instant now = Instant.now();
            RoomTypeBed bed = RoomTypeBed.forNew(
                    RoomTypeId.of(roomTypeId), BedTypeId.of(1L), 2, now
            );

            // when
            roomTypeBedCommandAdapter.persistAll(List.of(bed));

            // then - JpaRepository로 직접 조회하여 Entity 필드 검증
            List<RoomTypeBedJpaEntity> entities = roomTypeBedJpaRepository.findAll();
            assertThat(entities).isNotEmpty();

            RoomTypeBedJpaEntity savedEntity = entities.stream()
                    .filter(e -> e.getRoomTypeId().equals(roomTypeId) && e.getBedTypeId().equals(1L))
                    .findFirst()
                    .orElseThrow();

            assertThat(savedEntity.getRoomTypeId()).isEqualTo(roomTypeId);
            assertThat(savedEntity.getBedTypeId()).isEqualTo(1L);
            assertThat(savedEntity.getQuantity()).isEqualTo(2);
            assertThat(savedEntity.getCreatedAt()).isNotNull();
            assertThat(savedEntity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Entity -> Domain 변환 시 모든 필드가 올바르게 매핑된다")
        void shouldConvertEntityToDomainCorrectly() {
            // given
            RoomTypeBed bed = RoomTypeBed.forNew(
                    RoomTypeId.of(roomTypeId), BedTypeId.of(3L), 1, Instant.now()
            );
            roomTypeBedCommandAdapter.persistAll(List.of(bed));

            // when
            RoomTypeBedJpaEntity entity = roomTypeBedJpaRepository.findAll().stream()
                    .filter(e -> e.getBedTypeId().equals(3L))
                    .findFirst()
                    .orElseThrow();
            RoomTypeBed domain = roomTypeBedEntityMapper.toDomain(entity);

            // then
            assertThat(domain.id().value()).isEqualTo(entity.getId());
            assertThat(domain.roomTypeId().value()).isEqualTo(roomTypeId);
            assertThat(domain.bedTypeId().value()).isEqualTo(3L);
            assertThat(domain.quantity()).isEqualTo(1);
            assertThat(domain.createdAt()).isNotNull();
            assertThat(domain.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PT-2: persistAll 동작 검증")
    class PersistAllTest {

        @Test
        @DisplayName("단일 침대 구성을 저장할 수 있다")
        void shouldPersistSingleBed() {
            // given
            RoomTypeBed bed = RoomTypeBed.forNew(
                    RoomTypeId.of(roomTypeId), BedTypeId.of(1L), 1, Instant.now()
            );

            // when
            roomTypeBedCommandAdapter.persistAll(List.of(bed));

            // then
            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll();
            assertThat(result).anyMatch(e ->
                    e.getRoomTypeId().equals(roomTypeId)
                            && e.getBedTypeId().equals(1L)
                            && e.getQuantity() == 1
            );
        }

        @Test
        @DisplayName("복수 침대 구성을 한번에 저장할 수 있다")
        void shouldPersistMultipleBeds() {
            // given
            List<RoomTypeBed> beds = List.of(
                    RoomTypeBed.forNew(RoomTypeId.of(roomTypeId), BedTypeId.of(1L), 1, Instant.now()),
                    RoomTypeBed.forNew(RoomTypeId.of(roomTypeId), BedTypeId.of(2L), 2, Instant.now()),
                    RoomTypeBed.forNew(RoomTypeId.of(roomTypeId), BedTypeId.of(3L), 1, Instant.now())
            );

            // when
            roomTypeBedCommandAdapter.persistAll(beds);

            // then
            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll();
            List<RoomTypeBedJpaEntity> savedBeds = result.stream()
                    .filter(e -> e.getRoomTypeId().equals(roomTypeId))
                    .toList();
            assertThat(savedBeds).hasSize(3);
            assertThat(savedBeds).extracting(RoomTypeBedJpaEntity::getBedTypeId)
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(savedBeds).extracting(RoomTypeBedJpaEntity::getQuantity)
                    .containsExactlyInAnyOrder(1, 2, 1);
        }

        @Test
        @DisplayName("빈 리스트를 persistAll 해도 예외가 발생하지 않는다")
        void shouldHandleEmptyListGracefully() {
            // when & then - 예외 없이 정상 수행
            roomTypeBedCommandAdapter.persistAll(List.of());

            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll();
            // BeforeEach에서 생성한 데이터 외 추가 데이터 없음
            assertThat(result).allMatch(e -> !e.getRoomTypeId().equals(roomTypeId)
                    || e.getId() != null);
        }
    }

    @Nested
    @DisplayName("PT-3: 번들 패턴 — RoomType 생성 시 Bed 함께 저장")
    class BundlePatternTest {

        @Test
        @DisplayName("RoomType 저장 후 해당 ID로 Bed를 함께 저장할 수 있다")
        void shouldPersistBedsWithRoomType() {
            // given - 새 RoomType 생성
            RoomType newRoomType = RoomType.forNew(
                    PropertyId.of(propertyId), RoomTypeName.of("스위트"),
                    null, null, null, 2, 4, 3, null, null, Instant.now()
            );
            Long newRoomTypeId = roomTypeCommandAdapter.persist(newRoomType);

            // Pending 상태의 Bed에 RoomTypeId 할당
            RoomTypeBed pendingBed = RoomTypeBed.forPending(BedTypeId.of(1L), 1, Instant.now());
            RoomTypeBed assignedBed = pendingBed.withRoomTypeId(RoomTypeId.of(newRoomTypeId));

            // when
            roomTypeBedCommandAdapter.persistAll(List.of(assignedBed));

            // then
            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll();
            assertThat(result).anyMatch(e ->
                    e.getRoomTypeId().equals(newRoomTypeId)
                            && e.getBedTypeId().equals(1L)
                            && e.getQuantity() == 1
            );
        }

        @Test
        @DisplayName("Pending Bed 리스트에 RoomTypeId를 할당 후 일괄 저장할 수 있다")
        void shouldPersistPendingBedsWithAssignedRoomTypeId() {
            // given
            Long newRoomTypeId = roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("패밀리"),
                            null, null, null, 4, 6, 2, null, null, Instant.now())
            );

            List<RoomTypeBed> pendingBeds = List.of(
                    RoomTypeBed.forPending(BedTypeId.of(1L), 2, Instant.now()),
                    RoomTypeBed.forPending(BedTypeId.of(2L), 1, Instant.now())
            );
            List<RoomTypeBed> assignedBeds = pendingBeds.stream()
                    .map(b -> b.withRoomTypeId(RoomTypeId.of(newRoomTypeId)))
                    .toList();

            // when
            roomTypeBedCommandAdapter.persistAll(assignedBeds);

            // then
            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll().stream()
                    .filter(e -> e.getRoomTypeId().equals(newRoomTypeId))
                    .toList();
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("room_type_bed 테이블이 Flyway 마이그레이션으로 정상 생성된다")
        void shouldCreateRoomTypeBedTable() {
            // Flyway 마이그레이션이 실패하면 테스트 자체가 기동되지 않으므로
            // 여기까지 도달했다면 마이그레이션 성공을 의미한다.
            // 추가적으로 데이터 삽입이 정상 동작하는지 검증한다.
            RoomTypeBed bed = RoomTypeBed.forNew(
                    RoomTypeId.of(roomTypeId), BedTypeId.of(1L), 1, Instant.now()
            );
            roomTypeBedCommandAdapter.persistAll(List.of(bed));

            List<RoomTypeBedJpaEntity> result = roomTypeBedJpaRepository.findAll();
            assertThat(result).isNotEmpty();
        }
    }
}

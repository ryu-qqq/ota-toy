package com.ryuqq.otatoy.persistence.roomtype;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeStatus;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeQueryAdapter;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeQueryDslRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoomType Persistence Adapter 통합 테스트.
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
        RoomTypeCommandAdapter.class,
        RoomTypeQueryAdapter.class,
        RoomTypeEntityMapper.class,
        RoomTypeQueryDslRepository.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class
})
class RoomTypePersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    @Autowired
    private RoomTypeQueryAdapter roomTypeQueryAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    private Long propertyId;

    @BeforeEach
    void setUp() {
        // 객실 유형은 숙소에 속하므로 먼저 숙소를 생성
        Property property = PropertyFixture.aPropertyWithName("객실 테스트 호텔");
        propertyId = propertyCommandAdapter.persist(property);
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("RoomType 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            RoomType roomType = RoomType.forNew(
                    PropertyId.of(propertyId),
                    RoomTypeName.of("디럭스 더블"),
                    com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription.of("넓은 객실입니다"),
                    BigDecimal.valueOf(33.0),
                    "10평",
                    2, 4, 5,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    Instant.now()
            );

            // when
            Long savedId = roomTypeCommandAdapter.persist(roomType);
            Optional<RoomType> found = roomTypeQueryAdapter.findById(RoomTypeId.of(savedId));

            // then
            assertThat(found).isPresent();
            RoomType result = found.get();
            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.propertyId().value()).isEqualTo(propertyId);
            assertThat(result.name().value()).isEqualTo("디럭스 더블");
            assertThat(result.description().value()).isEqualTo("넓은 객실입니다");
            assertThat(result.areaSqm()).isEqualByComparingTo(BigDecimal.valueOf(33.0));
            assertThat(result.areaPyeong()).isEqualTo("10평");
            assertThat(result.baseOccupancy()).isEqualTo(2);
            assertThat(result.maxOccupancy()).isEqualTo(4);
            assertThat(result.baseInventory()).isEqualTo(5);
            assertThat(result.checkInTime()).isEqualTo(LocalTime.of(15, 0));
            assertThat(result.checkOutTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(result.status()).isEqualTo(RoomTypeStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("PT-2: CRUD 동작 검증")
    class CrudTest {

        @Test
        @DisplayName("persist 후 findById로 조회할 수 있다")
        void shouldPersistAndFindById() {
            // given
            RoomType roomType = RoomType.forNew(
                    PropertyId.of(propertyId), RoomTypeName.of("스탠다드 싱글"),
                    null, null, null, 1, 2, 3,
                    null, null, Instant.now()
            );

            // when
            Long savedId = roomTypeCommandAdapter.persist(roomType);
            Optional<RoomType> found = roomTypeQueryAdapter.findById(RoomTypeId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().name().value()).isEqualTo("스탠다드 싱글");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            Optional<RoomType> found = roomTypeQueryAdapter.findById(RoomTypeId.of(99999L));
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("existsById는 저장된 ID에 대해 true를 반환한다")
        void shouldReturnTrueForExistingId() {
            Long savedId = roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("테스트룸"),
                            null, null, null, 1, 2, 1, null, null, Instant.now()));
            assertThat(roomTypeQueryAdapter.existsById(RoomTypeId.of(savedId))).isTrue();
        }
    }

    @Nested
    @DisplayName("PT-3: QueryDSL 커스텀 쿼리 검증")
    class QueryDslTest {

        @Test
        @DisplayName("findByPropertyId는 해당 숙소의 모든 객실을 반환한다")
        void shouldFindByPropertyId() {
            // given
            roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("룸 A"),
                            null, null, null, 2, 4, 3, null, null, Instant.now()));
            roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("룸 B"),
                            null, null, null, 1, 2, 2, null, null, Instant.now()));

            // when
            List<RoomType> result = roomTypeQueryAdapter.findByPropertyId(PropertyId.of(propertyId));

            // then
            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("findActiveByPropertyIdAndMinOccupancy는 인원 조건을 충족하는 ACTIVE 객실만 반환한다")
        void shouldFindActiveByPropertyIdAndMinOccupancy() {
            // given - maxOccupancy 4인 객실과 maxOccupancy 2인 객실
            roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("큰 방"),
                            null, null, null, 2, 4, 3, null, null, Instant.now()));
            roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("작은 방"),
                            null, null, null, 1, 2, 2, null, null, Instant.now()));

            // when - 3명 이상 수용 가능한 객실만 조회
            List<RoomType> result = roomTypeQueryAdapter.findActiveByPropertyIdAndMinOccupancy(
                    PropertyId.of(propertyId), 3);

            // then
            assertThat(result).allMatch(rt -> rt.maxOccupancy() >= 3);
        }
    }
}

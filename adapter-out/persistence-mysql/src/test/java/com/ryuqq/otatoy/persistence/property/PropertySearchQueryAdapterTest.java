package com.ryuqq.otatoy.persistence.property;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.pricing.SourceType;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.inventory.adapter.InventoryCommandAdapter;
import com.ryuqq.otatoy.persistence.inventory.mapper.InventoryEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.adapter.RateCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyAmenityCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertySearchQueryAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAmenityEntityMapper;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertySearchQueryDslRepository;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertySearchQueryAdapter 통합 테스트.
 * 크로스 BC 조인 검색 (Property + RoomType + Inventory + Rate + RatePlan + PropertyAmenity)을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        PropertySearchQueryAdapter.class,
        PropertySearchQueryDslRepository.class,
        PropertyQueryDslRepository.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class,
        PropertyAmenityCommandAdapter.class,
        PropertyAmenityEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class,
        InventoryCommandAdapter.class,
        InventoryEntityMapper.class,
        RatePlanCommandAdapter.class,
        RatePlanEntityMapper.class,
        RateCommandAdapter.class,
        RateEntityMapper.class
})
class PropertySearchQueryAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PropertySearchQueryAdapter searchQueryAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private PropertyAmenityCommandAdapter amenityCommandAdapter;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    @Autowired
    private InventoryCommandAdapter inventoryCommandAdapter;

    @Autowired
    private RatePlanCommandAdapter ratePlanCommandAdapter;

    @Autowired
    private RateCommandAdapter rateCommandAdapter;

    private Long hotelAId;
    private Long hotelBId;

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 12);

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        // 호텔 A: 서울, 최대 4명, 재고 있음, 요금 100,000, 무료취소, WIFI 편의시설
        hotelAId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("호텔 A"));
        Long roomTypeAId = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(hotelAId), RoomTypeName.of("디럭스"),
                        null, null, null, 2, 4, 5, null, null, now));

        // 재고: 4/10, 4/11 가용
        List<Inventory> inventoriesA = List.of(
                Inventory.forNew(RoomTypeId.of(roomTypeAId), CHECK_IN, 5, now),
                Inventory.forNew(RoomTypeId.of(roomTypeAId), CHECK_IN.plusDays(1), 5, now)
        );
        inventoryCommandAdapter.persistAll(inventoriesA);

        // RatePlan + Rate
        Long ratePlanAId = ratePlanCommandAdapter.persist(
                RatePlan.forNew(RoomTypeId.of(roomTypeAId), RatePlanName.of("기본요금"),
                        SourceType.DIRECT, null,
                        CancellationPolicy.of(true, false, 3, "3일전 무료취소"),
                        PaymentPolicy.PREPAY, now));
        List<Rate> ratesA = List.of(
                Rate.forNew(RatePlanId.of(ratePlanAId), CHECK_IN, BigDecimal.valueOf(100_000), now),
                Rate.forNew(RatePlanId.of(ratePlanAId), CHECK_IN.plusDays(1), BigDecimal.valueOf(100_000), now)
        );
        rateCommandAdapter.persistAll(ratesA);

        // 편의시설: WIFI
        amenityCommandAdapter.persistAll(List.of(
                PropertyAmenity.forNew(PropertyId.of(hotelAId), AmenityType.WIFI,
                        AmenityName.of("와이파이"), Money.of(0), 1, now)
        ));

        // 호텔 B: 서울, 최대 2명, 재고 있음, 요금 200,000, 환불불가
        hotelBId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("호텔 B"));
        Long roomTypeBId = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(hotelBId), RoomTypeName.of("스탠다드"),
                        null, null, null, 1, 2, 3, null, null, now));

        List<Inventory> inventoriesB = List.of(
                Inventory.forNew(RoomTypeId.of(roomTypeBId), CHECK_IN, 3, now),
                Inventory.forNew(RoomTypeId.of(roomTypeBId), CHECK_IN.plusDays(1), 3, now)
        );
        inventoryCommandAdapter.persistAll(inventoriesB);

        Long ratePlanBId = ratePlanCommandAdapter.persist(
                RatePlan.forNew(RoomTypeId.of(roomTypeBId), RatePlanName.of("환불불가"),
                        SourceType.DIRECT, null,
                        CancellationPolicy.of(false, true, 0, "환불 불가"),
                        PaymentPolicy.PREPAY, now));
        List<Rate> ratesB = List.of(
                Rate.forNew(RatePlanId.of(ratePlanBId), CHECK_IN, BigDecimal.valueOf(200_000), now),
                Rate.forNew(RatePlanId.of(ratePlanBId), CHECK_IN.plusDays(1), BigDecimal.valueOf(200_000), now)
        );
        rateCommandAdapter.persistAll(ratesB);
    }

    @Nested
    @DisplayName("기본 검색")
    class BasicSearchTest {

        @Test
        @DisplayName("기본 조건으로 검색 시 모든 조건 충족 숙소가 반환된다")
        void shouldReturnMatchingProperties() {
            // given
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    20, null
            );

            // when
            SliceResult<Property> result = searchQueryAdapter.searchByCondition(criteria);

            // then
            assertThat(result.content()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("커서 페이지네이션이 동작한다")
        void shouldSupportCursorPagination() {
            // given - size=1로 첫 페이지
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    1, null
            );

            // when
            SliceResult<Property> firstPage = searchQueryAdapter.searchByCondition(criteria);

            // then
            assertThat(firstPage.content()).hasSize(1);
            assertThat(firstPage.hasNext()).isTrue();

            // 다음 페이지
            PropertySliceCriteria nextCriteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    1, firstPage.nextCursor()
            );

            SliceResult<Property> secondPage = searchQueryAdapter.searchByCondition(nextCriteria);
            assertThat(secondPage.content()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("인원 필터")
    class GuestsFilterTest {

        @Test
        @DisplayName("3명 수용 가능 객실이 있는 숙소만 반환한다")
        void shouldFilterByGuestCount() {
            // given - 호텔 A는 maxOccupancy=4, 호텔 B는 maxOccupancy=2
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 3,
                    null, null, List.of(),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    20, null
            );

            // when
            SliceResult<Property> result = searchQueryAdapter.searchByCondition(criteria);

            // then - 호텔 B(maxOccupancy=2)는 제외되어야 함
            assertThat(result.content()).allMatch(p -> !p.name().value().equals("호텔 B"));
        }
    }

    @Nested
    @DisplayName("무료취소 필터")
    class FreeCancellationFilterTest {

        @Test
        @DisplayName("무료취소 가능 숙소만 반환한다")
        void shouldFilterByFreeCancellation() {
            // given
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(),
                    true, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    20, null
            );

            // when
            SliceResult<Property> result = searchQueryAdapter.searchByCondition(criteria);

            // then - 호텔 B(환불불가)는 제외
            assertThat(result.content()).noneMatch(p -> p.name().value().equals("호텔 B"));
        }
    }

    @Nested
    @DisplayName("편의시설 필터")
    class AmenityFilterTest {

        @Test
        @DisplayName("WIFI 편의시설이 있는 숙소만 반환한다")
        void shouldFilterByAmenity() {
            // given
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(AmenityType.WIFI),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    20, null
            );

            // when
            SliceResult<Property> result = searchQueryAdapter.searchByCondition(criteria);

            // then - 호텔 B에는 WIFI가 없으므로 제외
            assertThat(result.content()).noneMatch(p -> p.name().value().equals("호텔 B"));
        }
    }

    @Nested
    @DisplayName("조건 불충족 시")
    class NoMatchTest {

        @Test
        @DisplayName("조건을 충족하는 숙소가 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyWhenNoMatch() {
            // given - 존재하지 않는 지역
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, "부산", null,
                    CHECK_IN, CHECK_OUT, 1,
                    null, null, List.of(),
                    false, null,
                    PropertySortKey.PRICE_LOW, SortDirection.ASC,
                    20, null
            );

            // when
            SliceResult<Property> result = searchQueryAdapter.searchByCondition(criteria);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }
}

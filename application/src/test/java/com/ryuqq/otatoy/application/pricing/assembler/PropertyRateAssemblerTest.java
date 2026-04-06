package com.ryuqq.otatoy.application.pricing.assembler;

import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.domain.inventory.Inventories;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.inventory.InventoryFixture;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyRateAssembler 단위 테스트.
 * 외부 의존이 없는 순수 조립 로직이므로 Mock 없이 테스트한다.
 * RoomType, RatePlan, Rate 캐시, Inventory를 조합하여 결과를 조립하는 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class PropertyRateAssemblerTest {

    PropertyRateAssembler assembler = new PropertyRateAssembler();

    private static final PropertyId PROPERTY_ID = PropertyId.of(100L);
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 12);

    private static RateFetchCriteria defaultCriteria() {
        return new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, 2);
    }

    @Nested
    @DisplayName("빈 결과")
    class EmptyResult {

        @Test
        @DisplayName("RoomTypes가 비어있으면 empty 결과를 반환한다")
        void shouldReturnEmptyWhenNoRoomTypes() {
            // given
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of());
            var ratePlans = RatePlans.from(List.of());
            var rateCache = Map.<String, BigDecimal>of();
            var inventories = Inventories.from(List.of());

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).isEmpty();
        }

        @Test
        @DisplayName("RatePlan이 없는 객실은 결과에 포함되지 않는다")
        void shouldExcludeRoomWithoutRatePlan() {
            // given
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of(RoomTypeFixture.reconstitutedRoomTypeWithId(99L)));
            var ratePlans = RatePlans.from(List.of()); // roomTypeId=99에 대한 RatePlan 없음
            var rateCache = Map.<String, BigDecimal>of();
            var inventories = Inventories.from(List.of());

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then
            assertThat(result.roomRates()).isEmpty();
        }
    }

    @Nested
    @DisplayName("재고 미가용 처리")
    class UnavailableInventory {

        @Test
        @DisplayName("숙박 기간 중 하루라도 재고가 없으면 해당 RatePlan은 결과에서 제외된다")
        void shouldExcludeRatePlanWhenInventoryUnavailableOnAnyDate() {
            // given
            var criteria = defaultCriteria();
            RoomType roomType = RoomTypeFixture.reconstitutedRoomTypeWithId(1L);
            var roomTypes = RoomTypes.from(List.of(roomType));

            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var ratePlans = RatePlans.from(List.of(ratePlan));

            // 4/10 캐시 있지만, 4/11에 재고 없음
            var rateCache = Map.of(
                "1:2026-04-10", BigDecimal.valueOf(100_000),
                "1:2026-04-11", BigDecimal.valueOf(100_000)
            );

            // 4/10은 재고 있고, 4/11은 재고 없음 (exhausted)
            Inventory availableInv = InventoryFixture.reconstituted(1L, 1L, LocalDate.of(2026, 4, 10), 10, 5, false, 1);
            Inventory exhaustedInv = InventoryFixture.reconstituted(2L, 1L, LocalDate.of(2026, 4, 11), 10, 0, false, 1);
            var inventories = Inventories.from(List.of(availableInv, exhaustedInv));

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then -- 재고 미가용이므로 결과에서 제외
            assertThat(result.roomRates()).isEmpty();
        }

        @Test
        @DisplayName("특정 날짜에 Inventory 데이터 자체가 없으면 해당 RatePlan은 결과에서 제외된다")
        void shouldExcludeRatePlanWhenInventoryDataMissing() {
            // given
            var criteria = defaultCriteria();
            RoomType roomType = RoomTypeFixture.reconstitutedRoomTypeWithId(1L);
            var roomTypes = RoomTypes.from(List.of(roomType));

            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var ratePlans = RatePlans.from(List.of(ratePlan));

            var rateCache = Map.of(
                "1:2026-04-10", BigDecimal.valueOf(100_000),
                "1:2026-04-11", BigDecimal.valueOf(100_000)
            );

            // 4/10만 재고 데이터 존재, 4/11은 데이터 자체가 없음
            Inventory availableInv = InventoryFixture.reconstituted(1L, 1L, LocalDate.of(2026, 4, 10), 10, 5, false, 1);
            var inventories = Inventories.from(List.of(availableInv));

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then
            assertThat(result.roomRates()).isEmpty();
        }
    }

    @Nested
    @DisplayName("정상 조립")
    class SuccessfulAssembly {

        @Test
        @DisplayName("모든 날짜에 재고가 가용하면 RoomRateSummary가 생성된다")
        void shouldAssembleRoomRateSummaryWhenAllDatesAvailable() {
            // given
            var criteria = defaultCriteria();
            RoomType roomType = RoomTypeFixture.reconstitutedRoomTypeWithId(1L);
            var roomTypes = RoomTypes.from(List.of(roomType));

            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var ratePlans = RatePlans.from(List.of(ratePlan));

            var rateCache = Map.of(
                "1:2026-04-10", BigDecimal.valueOf(100_000),
                "1:2026-04-11", BigDecimal.valueOf(120_000)
            );

            Inventory inv1 = InventoryFixture.reconstituted(1L, 1L, LocalDate.of(2026, 4, 10), 10, 5, false, 1);
            Inventory inv2 = InventoryFixture.reconstituted(2L, 1L, LocalDate.of(2026, 4, 11), 10, 3, false, 1);
            var inventories = Inventories.from(List.of(inv1, inv2));

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).hasSize(1);
            var summary = result.roomRates().get(0);
            assertThat(summary.roomTypeId()).isEqualTo(roomType.id());
            assertThat(summary.ratePlanId()).isEqualTo(ratePlan.id());
            assertThat(summary.dailyRates()).hasSize(2);
            assertThat(summary.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(220_000));
        }

        @Test
        @DisplayName("캐시에 가격이 없으면 0원으로 처리된다")
        void shouldUsesZeroPriceWhenCacheMiss() {
            // given
            var criteria = defaultCriteria();
            RoomType roomType = RoomTypeFixture.reconstitutedRoomTypeWithId(1L);
            var roomTypes = RoomTypes.from(List.of(roomType));

            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var ratePlans = RatePlans.from(List.of(ratePlan));

            // 캐시에 가격 정보 없음
            var rateCache = Map.<String, BigDecimal>of();

            Inventory inv1 = InventoryFixture.reconstituted(1L, 1L, LocalDate.of(2026, 4, 10), 10, 5, false, 1);
            Inventory inv2 = InventoryFixture.reconstituted(2L, 1L, LocalDate.of(2026, 4, 11), 10, 3, false, 1);
            var inventories = Inventories.from(List.of(inv1, inv2));

            // when
            CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);

            // then
            assertThat(result.roomRates()).hasSize(1);
            assertThat(result.roomRates().get(0).totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}

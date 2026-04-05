package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryReadManager;
import com.ryuqq.otatoy.application.pricing.dto.query.FetchRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.query.FetchRateQueryFixture;
import com.ryuqq.otatoy.application.pricing.dto.result.PropertyRateResult;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.manager.RateReadManager;
import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.pricing.RateId;
import com.ryuqq.otatoy.domain.pricing.SourceType;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeStatus;
import com.ryuqq.otatoy.domain.inventory.InventoryId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * FetchRateService 단위 테스트.
 * PropertyReadManager, RoomTypeReadManager, RatePlanReadManager,
 * RateReadManager, InventoryReadManager를 Mock으로 대체하여
 * Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class FetchRateServiceTest {

    @Mock
    PropertyReadManager propertyReadManager;

    @Mock
    RoomTypeReadManager roomTypeReadManager;

    @Mock
    RatePlanReadManager ratePlanReadManager;

    @Mock
    RateReadManager rateReadManager;

    @Mock
    InventoryReadManager inventoryReadManager;

    @InjectMocks
    FetchRateService service;

    // === 테스트 상수 ===
    static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    static final RoomTypeId ROOM_TYPE_ID_1 = RoomTypeId.of(10L);
    static final RatePlanId RATE_PLAN_ID_1 = RatePlanId.of(100L);
    static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 12);

    // === 헬퍼 메서드 ===

    private RoomType activeRoomType(RoomTypeId id, int maxOccupancy) {
        return RoomType.reconstitute(
                id, PROPERTY_ID, RoomTypeName.of("객실"),
                RoomTypeDescription.of("설명"),
                BigDecimal.valueOf(33), "10평",
                2, maxOccupancy, 5,
                LocalTime.of(15, 0), LocalTime.of(11, 0),
                RoomTypeStatus.ACTIVE, NOW, NOW
        );
    }

    private RoomType inactiveRoomType(RoomTypeId id) {
        return RoomType.reconstitute(
                id, PROPERTY_ID, RoomTypeName.of("비활성 객실"),
                RoomTypeDescription.of("설명"),
                BigDecimal.valueOf(33), "10평",
                2, 4, 5,
                LocalTime.of(15, 0), LocalTime.of(11, 0),
                RoomTypeStatus.INACTIVE, NOW, NOW
        );
    }

    private RatePlan aRatePlan(RatePlanId id, RoomTypeId roomTypeId) {
        return RatePlan.reconstitute(
                id, roomTypeId, RatePlanName.of("기본 요금제"),
                SourceType.DIRECT, null,
                CancellationPolicy.of(true, false, 3, "무료 취소"),
                PaymentPolicy.PREPAY, NOW, NOW
        );
    }

    private Rate aRate(RatePlanId ratePlanId, LocalDate date, BigDecimal price) {
        return Rate.reconstitute(
                RateId.of(date.getDayOfMonth() * 10L), ratePlanId,
                date, price, NOW, NOW
        );
    }

    private Inventory availableInventory(RoomTypeId roomTypeId, LocalDate date, int available) {
        return Inventory.reconstitute(
                InventoryId.of(date.getDayOfMonth() * 100L), roomTypeId, date,
                10, available, false, 1, NOW, NOW
        );
    }

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("숙소의 객실별 날짜별 요금과 재고를 조회하여 결과를 반환한다")
        void shouldReturnRoomRateSummariesWithDailyRates() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            RoomType roomType = activeRoomType(ROOM_TYPE_ID_1, 4);
            RatePlan ratePlan = aRatePlan(RATE_PLAN_ID_1, ROOM_TYPE_ID_1);
            Rate rate1 = aRate(RATE_PLAN_ID_1, CHECK_IN, BigDecimal.valueOf(100_000));
            Rate rate2 = aRate(RATE_PLAN_ID_1, CHECK_IN.plusDays(1), BigDecimal.valueOf(120_000));
            Inventory inv1 = availableInventory(ROOM_TYPE_ID_1, CHECK_IN, 5);
            Inventory inv2 = availableInventory(ROOM_TYPE_ID_1, CHECK_IN.plusDays(1), 3);

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
            given(ratePlanReadManager.findByRoomTypeIds(List.of(ROOM_TYPE_ID_1))).willReturn(List.of(ratePlan));
            given(rateReadManager.findByRatePlanIdsAndDateRange(
                    List.of(RATE_PLAN_ID_1), CHECK_IN, CHECK_OUT))
                    .willReturn(List.of(rate1, rate2));
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(
                    List.of(ROOM_TYPE_ID_1), CHECK_IN, CHECK_OUT))
                    .willReturn(List.of(inv1, inv2));

            // when
            PropertyRateResult result = service.execute(query);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).hasSize(1);
            assertThat(result.roomRates().get(0).roomTypeId()).isEqualTo(ROOM_TYPE_ID_1);
            assertThat(result.roomRates().get(0).dailyRates()).hasSize(2);
            assertThat(result.roomRates().get(0).totalPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(220_000));
        }
    }

    @Nested
    @DisplayName("숙소 미존재 흐름")
    class PropertyNotFound {

        @Test
        @DisplayName("존재하지 않는 숙소로 조회 시 PropertyNotFoundException이 전파된다")
        void shouldPropagatePropertyNotFoundException() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            willThrow(new PropertyNotFoundException()).given(propertyReadManager).verifyExists(PROPERTY_ID);

            // when & then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("숙소 미존재 시 객실/요금 조회가 호출되지 않는다")
        void shouldNotQueryRoomTypesWhenPropertyNotFound() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            willThrow(new PropertyNotFoundException()).given(propertyReadManager).verifyExists(PROPERTY_ID);

            // when
            try {
                service.execute(query);
            } catch (PropertyNotFoundException ignored) {
            }

            // then
            then(roomTypeReadManager).should(never()).findByPropertyId(any());
            then(ratePlanReadManager).should(never()).findByRoomTypeIds(anyList());
        }
    }

    @Nested
    @DisplayName("객실 필터링 흐름")
    class RoomTypeFiltering {

        @Test
        @DisplayName("maxOccupancy < guests인 객실은 결과에서 제외된다")
        void shouldFilterOutRoomTypeWithInsufficientCapacity() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery(); // guests=2
            RoomType tooSmall = activeRoomType(RoomTypeId.of(11L), 1); // maxOccupancy=1
            RoomType adequate = activeRoomType(ROOM_TYPE_ID_1, 4);

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID))
                    .willReturn(List.of(tooSmall, adequate));
            given(ratePlanReadManager.findByRoomTypeIds(List.of(ROOM_TYPE_ID_1)))
                    .willReturn(List.of());

            // when
            PropertyRateResult result = service.execute(query);

            // then -- tooSmall 객실의 RatePlan은 조회하지 않아야 함
            assertThat(result.roomRates()).isEmpty();
        }

        @Test
        @DisplayName("비활성 객실은 결과에서 제외된다")
        void shouldFilterOutInactiveRoomType() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            RoomType inactive = inactiveRoomType(RoomTypeId.of(11L));

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID))
                    .willReturn(List.of(inactive));

            // when
            PropertyRateResult result = service.execute(query);

            // then
            assertThat(result.roomRates()).isEmpty();
        }

        @Test
        @DisplayName("적합한 객실이 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyWhenNoSuitableRoomTypes() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID)).willReturn(List.of());

            // when
            PropertyRateResult result = service.execute(query);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).isEmpty();
        }
    }

    @Nested
    @DisplayName("재고 가용성 흐름")
    class InventoryAvailability {

        @Test
        @DisplayName("특정 날짜에 재고가 없으면 해당 RatePlan은 결과에서 제외된다")
        void shouldExcludeRatePlanWhenInventoryUnavailable() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            RoomType roomType = activeRoomType(ROOM_TYPE_ID_1, 4);
            RatePlan ratePlan = aRatePlan(RATE_PLAN_ID_1, ROOM_TYPE_ID_1);
            Rate rate1 = aRate(RATE_PLAN_ID_1, CHECK_IN, BigDecimal.valueOf(100_000));
            Rate rate2 = aRate(RATE_PLAN_ID_1, CHECK_IN.plusDays(1), BigDecimal.valueOf(120_000));
            // 첫째 날 재고 있음, 둘째 날 재고 소진
            Inventory inv1 = availableInventory(ROOM_TYPE_ID_1, CHECK_IN, 5);
            Inventory inv2 = Inventory.reconstitute(
                    InventoryId.of(200L), ROOM_TYPE_ID_1, CHECK_IN.plusDays(1),
                    10, 0, false, 1, NOW, NOW
            );

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
            given(ratePlanReadManager.findByRoomTypeIds(List.of(ROOM_TYPE_ID_1))).willReturn(List.of(ratePlan));
            given(rateReadManager.findByRatePlanIdsAndDateRange(
                    List.of(RATE_PLAN_ID_1), CHECK_IN, CHECK_OUT))
                    .willReturn(List.of(rate1, rate2));
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(
                    List.of(ROOM_TYPE_ID_1), CHECK_IN, CHECK_OUT))
                    .willReturn(List.of(inv1, inv2));

            // when
            PropertyRateResult result = service.execute(query);

            // then -- 모든 날짜에 재고가 있어야 결과에 포함되므로 빈 결과
            assertThat(result.roomRates()).isEmpty();
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("verifyExists -> findByPropertyId -> findByRoomTypeIds -> findByRatePlanIds -> findInventory 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            RoomType roomType = activeRoomType(ROOM_TYPE_ID_1, 4);
            RatePlan ratePlan = aRatePlan(RATE_PLAN_ID_1, ROOM_TYPE_ID_1);

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
            given(ratePlanReadManager.findByRoomTypeIds(List.of(ROOM_TYPE_ID_1))).willReturn(List.of(ratePlan));
            given(rateReadManager.findByRatePlanIdsAndDateRange(anyList(), any(), any())).willReturn(List.of());
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(anyList(), any(), any())).willReturn(List.of());

            // when
            service.execute(query);

            // then
            InOrder inOrder = inOrder(propertyReadManager, roomTypeReadManager, ratePlanReadManager, rateReadManager, inventoryReadManager);
            inOrder.verify(propertyReadManager).verifyExists(PROPERTY_ID);
            inOrder.verify(roomTypeReadManager).findByPropertyId(PROPERTY_ID);
            inOrder.verify(ratePlanReadManager).findByRoomTypeIds(List.of(ROOM_TYPE_ID_1));
            inOrder.verify(rateReadManager).findByRatePlanIdsAndDateRange(anyList(), any(), any());
            inOrder.verify(inventoryReadManager).findByRoomTypeIdsAndDateRange(anyList(), any(), any());
        }
    }

    @Nested
    @DisplayName("RatePlan 없음 흐름")
    class NoRatePlan {

        @Test
        @DisplayName("객실에 RatePlan이 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyWhenNoRatePlans() {
            // given
            FetchRateQuery query = FetchRateQueryFixture.aFetchRateQuery();
            RoomType roomType = activeRoomType(ROOM_TYPE_ID_1, 4);

            willDoNothing().given(propertyReadManager).verifyExists(PROPERTY_ID);
            given(roomTypeReadManager.findByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
            given(ratePlanReadManager.findByRoomTypeIds(List.of(ROOM_TYPE_ID_1))).willReturn(List.of());

            // when
            PropertyRateResult result = service.execute(query);

            // then
            assertThat(result.roomRates()).isEmpty();
        }
    }
}

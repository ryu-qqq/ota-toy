package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryReadManager;
import com.ryuqq.otatoy.application.pricing.assembler.PropertyRateAssembler;
import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.application.pricing.factory.RateCriteriaFactory;
import com.ryuqq.otatoy.application.pricing.manager.RateCacheManager;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.inventory.Inventories;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * CustomerGetRateService 단위 테스트.
 * Service의 오케스트레이션 로직을 검증한다.
 * 조회 흐름: CriteriaFactory -> RoomTypeReadManager -> RatePlanReadManager
 *           -> RateCacheManager -> InventoryReadManager -> Assembler
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class CustomerGetRateServiceTest {

    @Mock RateCriteriaFactory criteriaFactory;
    @Mock RoomTypeReadManager roomTypeReadManager;
    @Mock RatePlanReadManager ratePlanReadManager;
    @Mock RateCacheManager rateCacheManager;
    @Mock InventoryReadManager inventoryReadManager;
    @Mock PropertyRateAssembler assembler;

    private CustomerGetRateService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        service = new CustomerGetRateService(criteriaFactory, roomTypeReadManager,
                ratePlanReadManager, rateCacheManager, inventoryReadManager, assembler, meterRegistry);
    }

    // -- 헬퍼 --

    private static final PropertyId PROPERTY_ID = PropertyId.of(100L);
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 12);
    private static final int GUESTS = 2;

    private static CustomerGetRateQuery defaultQuery() {
        return new CustomerGetRateQuery(PROPERTY_ID, CHECK_IN, CHECK_OUT, GUESTS);
    }

    private static RateFetchCriteria defaultCriteria() {
        return new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, GUESTS);
    }

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("수용 가능한 객실과 요금 정책이 존재할 때 Assembler 결과를 반환한다")
        void shouldReturnAssemblerResult() {
            // given
            var query = defaultQuery();
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of(RoomTypeFixture.reconstitutedRoomTypeWithId(1L)));
            var ratePlans = RatePlans.from(List.of(PricingFixtures.reconstitutedRatePlan(1L)));
            var rateCache = Map.of("1:2026-04-10", BigDecimal.valueOf(100_000));
            var inventories = Inventories.from(List.of());
            var expectedResult = mock(CustomerPropertyRateResult.class);

            given(criteriaFactory.create(query)).willReturn(criteria);
            given(roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests()))
                .willReturn(roomTypes);
            given(ratePlanReadManager.findByRoomTypeIds(roomTypes.roomTypeIds())).willReturn(ratePlans);
            given(rateCacheManager.getRates(ratePlans.ratePlanIds(), criteria.stayDates())).willReturn(rateCache);
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(roomTypes.roomTypeIds(), CHECK_IN, CHECK_OUT))
                .willReturn(inventories);
            given(assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories))
                .willReturn(expectedResult);

            // when
            CustomerPropertyRateResult result = service.execute(query);

            // then
            assertThat(result).isSameAs(expectedResult);
        }
    }

    @Nested
    @DisplayName("빈 결과 반환 - 조기 종료")
    class EarlyReturn {

        @Test
        @DisplayName("수용 가능한 객실이 없으면 empty 결과를 반환하고 후속 Manager를 호출하지 않는다")
        void shouldReturnEmptyWhenNoRoomTypes() {
            // given
            var query = defaultQuery();
            var criteria = defaultCriteria();
            var emptyRoomTypes = RoomTypes.from(List.of());

            given(criteriaFactory.create(query)).willReturn(criteria);
            given(roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests()))
                .willReturn(emptyRoomTypes);

            // when
            CustomerPropertyRateResult result = service.execute(query);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).isEmpty();
            then(ratePlanReadManager).shouldHaveNoInteractions();
            then(rateCacheManager).shouldHaveNoInteractions();
            then(inventoryReadManager).shouldHaveNoInteractions();
            then(assembler).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("요금 정책이 없으면 empty 결과를 반환하고 캐시/재고 조회를 하지 않는다")
        void shouldReturnEmptyWhenNoRatePlans() {
            // given
            var query = defaultQuery();
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of(RoomTypeFixture.reconstitutedRoomTypeWithId(1L)));
            var emptyRatePlans = RatePlans.from(List.of());

            given(criteriaFactory.create(query)).willReturn(criteria);
            given(roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests()))
                .willReturn(roomTypes);
            given(ratePlanReadManager.findByRoomTypeIds(roomTypes.roomTypeIds())).willReturn(emptyRatePlans);

            // when
            CustomerPropertyRateResult result = service.execute(query);

            // then
            assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(result.roomRates()).isEmpty();
            then(rateCacheManager).shouldHaveNoInteractions();
            then(inventoryReadManager).shouldHaveNoInteractions();
            then(assembler).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("CriteriaFactory -> RoomTypeReadManager -> RatePlanReadManager -> RateCacheManager -> InventoryReadManager -> Assembler 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            var query = defaultQuery();
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of(RoomTypeFixture.reconstitutedRoomTypeWithId(1L)));
            var ratePlans = RatePlans.from(List.of(PricingFixtures.reconstitutedRatePlan(1L)));
            var rateCache = Map.<String, BigDecimal>of();
            var inventories = Inventories.from(List.of());
            var expectedResult = CustomerPropertyRateResult.empty(PROPERTY_ID);

            given(criteriaFactory.create(query)).willReturn(criteria);
            given(roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests()))
                .willReturn(roomTypes);
            given(ratePlanReadManager.findByRoomTypeIds(roomTypes.roomTypeIds())).willReturn(ratePlans);
            given(rateCacheManager.getRates(ratePlans.ratePlanIds(), criteria.stayDates())).willReturn(rateCache);
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(roomTypes.roomTypeIds(), CHECK_IN, CHECK_OUT))
                .willReturn(inventories);
            given(assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories))
                .willReturn(expectedResult);

            // when
            service.execute(query);

            // then
            InOrder inOrder = inOrder(criteriaFactory, roomTypeReadManager, ratePlanReadManager,
                rateCacheManager, inventoryReadManager, assembler);
            inOrder.verify(criteriaFactory).create(query);
            inOrder.verify(roomTypeReadManager).findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests());
            inOrder.verify(ratePlanReadManager).findByRoomTypeIds(roomTypes.roomTypeIds());
            inOrder.verify(rateCacheManager).getRates(ratePlans.ratePlanIds(), criteria.stayDates());
            inOrder.verify(inventoryReadManager).findByRoomTypeIdsAndDateRange(roomTypes.roomTypeIds(), CHECK_IN, CHECK_OUT);
            inOrder.verify(assembler).toResult(criteria, roomTypes, ratePlans, rateCache, inventories);
        }
    }

    @Nested
    @DisplayName("Assembler 인자 검증")
    class AssemblerArgumentVerification {

        @Test
        @DisplayName("각 Manager에서 반환한 결과가 Assembler에 그대로 전달된다")
        void shouldPassExactArgumentsToAssembler() {
            // given
            var query = defaultQuery();
            var criteria = defaultCriteria();
            var roomTypes = RoomTypes.from(List.of(RoomTypeFixture.reconstitutedRoomTypeWithId(1L)));
            var ratePlans = RatePlans.from(List.of(PricingFixtures.reconstitutedRatePlan(1L)));
            var rateCache = Map.of("1:2026-04-10", BigDecimal.valueOf(100_000));
            var inventories = Inventories.from(List.of());
            var expectedResult = CustomerPropertyRateResult.empty(PROPERTY_ID);

            given(criteriaFactory.create(query)).willReturn(criteria);
            given(roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(criteria.propertyId(), criteria.guests()))
                .willReturn(roomTypes);
            given(ratePlanReadManager.findByRoomTypeIds(roomTypes.roomTypeIds())).willReturn(ratePlans);
            given(rateCacheManager.getRates(ratePlans.ratePlanIds(), criteria.stayDates())).willReturn(rateCache);
            given(inventoryReadManager.findByRoomTypeIdsAndDateRange(roomTypes.roomTypeIds(), CHECK_IN, CHECK_OUT))
                .willReturn(inventories);
            given(assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories))
                .willReturn(expectedResult);

            // when
            service.execute(query);

            // then -- 동일한 객체 인스턴스들이 전달되었는지 검증
            then(assembler).should().toResult(criteria, roomTypes, ratePlans, rateCache, inventories);
        }
    }
}

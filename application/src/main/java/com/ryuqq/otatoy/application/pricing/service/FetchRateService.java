package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryReadManager;
import com.ryuqq.otatoy.application.pricing.dto.query.FetchRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.PropertyRateResult;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.manager.RateReadManager;
import com.ryuqq.otatoy.application.pricing.port.in.FetchRateUseCase;
import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 요금 조회 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 Manager에서 관리한다.
 *
 * 조회 흐름:
 * 1. 숙소 존재 확인
 * 2. 해당 숙소의 객실 유형 조회 (인원 필터)
 * 3. 객실별 RatePlan 조회
 * 4. RatePlan별 날짜별 Rate 조회
 * 5. 객실별 날짜별 Inventory 조회
 * 6. 결과 조립
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class FetchRateService implements FetchRateUseCase {

    private final PropertyReadManager propertyReadManager;
    private final RoomTypeReadManager roomTypeReadManager;
    private final RatePlanReadManager ratePlanReadManager;
    private final RateReadManager rateReadManager;
    private final InventoryReadManager inventoryReadManager;

    public FetchRateService(PropertyReadManager propertyReadManager,
                             RoomTypeReadManager roomTypeReadManager,
                             RatePlanReadManager ratePlanReadManager,
                             RateReadManager rateReadManager,
                             InventoryReadManager inventoryReadManager) {
        this.propertyReadManager = propertyReadManager;
        this.roomTypeReadManager = roomTypeReadManager;
        this.ratePlanReadManager = ratePlanReadManager;
        this.rateReadManager = rateReadManager;
        this.inventoryReadManager = inventoryReadManager;
    }

    @Override
    public PropertyRateResult execute(FetchRateQuery query) {
        // Query DTO -> Domain Criteria 변환 (Service에서 직접 수행)
        RateFetchCriteria criteria = new RateFetchCriteria(
                query.propertyId(), query.checkIn(), query.checkOut(), query.guests()
        );

        // 1. 숙소 존재 확인 (다른 BC ReadManager -- 읽기 OK, APP-BC-001)
        propertyReadManager.verifyExists(criteria.propertyId());

        // 2. 해당 숙소의 객실 유형 조회 + 인원 필터
        List<RoomType> roomTypes = roomTypeReadManager.findByPropertyId(criteria.propertyId());
        List<RoomType> filteredRoomTypes = roomTypes.stream()
                .filter(rt -> rt.maxOccupancy() >= criteria.guests())
                .filter(RoomType::isActive)
                .toList();

        if (filteredRoomTypes.isEmpty()) {
            return PropertyRateResult.of(criteria.propertyId(), List.of());
        }

        // 3. 객실별 RatePlan 조회
        List<RoomTypeId> roomTypeIds = filteredRoomTypes.stream()
                .map(RoomType::id)
                .toList();
        List<RatePlan> ratePlans = ratePlanReadManager.findByRoomTypeIds(roomTypeIds);

        if (ratePlans.isEmpty()) {
            return PropertyRateResult.of(criteria.propertyId(), List.of());
        }

        // 4. RatePlan별 날짜별 Rate 조회
        List<RatePlanId> ratePlanIds = ratePlans.stream()
                .map(RatePlan::id)
                .toList();
        List<Rate> rates = rateReadManager.findByRatePlanIdsAndDateRange(
                ratePlanIds, criteria.checkIn(), criteria.checkOut());

        // 5. 객실별 날짜별 Inventory 조회
        List<Inventory> inventories = inventoryReadManager.findByRoomTypeIdsAndDateRange(
                roomTypeIds, criteria.checkIn(), criteria.checkOut());

        // 6. 결과 조립
        return assembleResult(criteria, filteredRoomTypes, ratePlans, rates, inventories);
    }

    private PropertyRateResult assembleResult(RateFetchCriteria criteria,
                                               List<RoomType> roomTypes,
                                               List<RatePlan> ratePlans,
                                               List<Rate> rates,
                                               List<Inventory> inventories) {
        // RatePlan을 RoomTypeId 기준으로 그룹핑
        Map<RoomTypeId, List<RatePlan>> ratePlansByRoomType = ratePlans.stream()
                .collect(Collectors.groupingBy(RatePlan::roomTypeId));

        // Rate를 RatePlanId 기준으로 그룹핑
        Map<RatePlanId, List<Rate>> ratesByRatePlan = rates.stream()
                .collect(Collectors.groupingBy(Rate::ratePlanId));

        // Inventory를 RoomTypeId + 날짜 기준으로 매핑
        Map<RoomTypeId, Map<LocalDate, Inventory>> inventoryMap = inventories.stream()
                .collect(Collectors.groupingBy(
                        Inventory::roomTypeId,
                        Collectors.toMap(Inventory::inventoryDate, inv -> inv, (a, b) -> a)
                ));

        List<RoomRateSummary> roomRateSummaries = new ArrayList<>();
        List<LocalDate> stayDates = criteria.stayDates();

        for (RoomType roomType : roomTypes) {
            List<RatePlan> roomRatePlans = ratePlansByRoomType.getOrDefault(roomType.id(), List.of());
            Map<LocalDate, Inventory> roomInventory = inventoryMap.getOrDefault(roomType.id(), Map.of());

            for (RatePlan ratePlan : roomRatePlans) {
                List<Rate> planRates = ratesByRatePlan.getOrDefault(ratePlan.id(), List.of());
                Map<LocalDate, Rate> rateByDate = planRates.stream()
                        .collect(Collectors.toMap(Rate::rateDate, r -> r, (a, b) -> a));

                List<DailyRate> dailyRates = new ArrayList<>();
                BigDecimal totalPrice = BigDecimal.ZERO;
                boolean allDatesAvailable = true;

                for (LocalDate date : stayDates) {
                    Rate rate = rateByDate.get(date);
                    Inventory inventory = roomInventory.get(date);

                    BigDecimal price = rate != null ? rate.basePrice() : BigDecimal.ZERO;
                    int availableCount = inventory != null ? inventory.availableCount() : 0;
                    boolean available = inventory != null && inventory.isAvailable();

                    dailyRates.add(DailyRate.of(date, price, availableCount, available));
                    totalPrice = totalPrice.add(price);

                    if (!available) {
                        allDatesAvailable = false;
                    }
                }

                // 모든 날짜에 재고가 있는 경우에만 결과에 포함
                if (allDatesAvailable && !dailyRates.isEmpty()) {
                    roomRateSummaries.add(RoomRateSummary.of(
                            roomType.id(), roomType.name(), roomType.maxOccupancy(),
                            ratePlan.id(), ratePlan.name(), ratePlan.cancellationPolicy(),
                            dailyRates, totalPrice
                    ));
                }
            }
        }

        return PropertyRateResult.of(criteria.propertyId(), roomRateSummaries);
    }
}

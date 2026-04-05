package com.ryuqq.otatoy.application.pricing.assembler;

import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.domain.inventory.Inventories;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 요금 조회 결과를 조립하는 Assembler.
 * Service에서 분리하여 조립 로직만 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertyRateAssembler {

    public CustomerPropertyRateResult toResult(RateFetchCriteria criteria,
                                                RoomTypes roomTypes,
                                                RatePlans ratePlans,
                                                Map<String, BigDecimal> rateCache,
                                                Inventories inventories) {
        if (roomTypes.isEmpty()) {
            return CustomerPropertyRateResult.empty(criteria.propertyId());
        }

        Map<RoomTypeId, List<RatePlan>> ratePlansByRoomType = ratePlans.groupByRoomTypeId();
        Map<RoomTypeId, Map<LocalDate, Inventory>> inventoryMap = inventories.groupByRoomTypeAndDate();

        List<LocalDate> stayDates = criteria.stayDates();
        List<RoomRateSummary> roomRateSummaries = new ArrayList<>();

        for (RoomType roomType : roomTypes.items()) {
            List<RatePlan> roomRatePlans = ratePlansByRoomType.getOrDefault(roomType.id(), List.of());
            Map<LocalDate, Inventory> roomInventory = inventoryMap.getOrDefault(roomType.id(), Map.of());

            for (RatePlan ratePlan : roomRatePlans) {
                RoomRateSummary summary = assembleSingleRatePlan(
                        roomType, ratePlan, stayDates, roomInventory, rateCache);
                if (summary != null) {
                    roomRateSummaries.add(summary);
                }
            }
        }

        return CustomerPropertyRateResult.of(criteria.propertyId(), roomRateSummaries);
    }

    private RoomRateSummary assembleSingleRatePlan(RoomType roomType, RatePlan ratePlan,
                                                    List<LocalDate> stayDates,
                                                    Map<LocalDate, Inventory> roomInventory,
                                                    Map<String, BigDecimal> rateCache) {
        List<DailyRate> dailyRates = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (LocalDate date : stayDates) {
            String cacheKey = ratePlan.id().value() + ":" + date;
            BigDecimal price = rateCache.getOrDefault(cacheKey, BigDecimal.ZERO);

            Inventory inventory = roomInventory.get(date);
            int availableCount = inventory != null ? inventory.availableCount() : 0;
            boolean available = inventory != null && inventory.isAvailable();

            if (!available) {
                return null;
            }

            dailyRates.add(DailyRate.of(date, price, availableCount, available));
            totalPrice = totalPrice.add(price);
        }

        if (dailyRates.isEmpty()) {
            return null;
        }

        return RoomRateSummary.of(
                roomType.id(), roomType.name(), roomType.maxOccupancy(),
                ratePlan.id(), ratePlan.name(), ratePlan.cancellationPolicy(),
                dailyRates, totalPrice
        );
    }
}

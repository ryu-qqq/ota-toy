package com.ryuqq.otatoy.application.supplier.sync;

import com.ryuqq.otatoy.application.supplier.dto.SupplierRateData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierRateSyncBundle;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 외부 요금·가용성 데이터와 내부 매핑을 조합해 저장용 Rate·Inventory·캐시 항목을 만든다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRateSyncAssembler {

    public SupplierRateSyncBundle assemble(List<SupplierRateData> rateDataList,
                                           List<SupplierRoomType> roomTypeMappings,
                                           RatePlans ratePlans,
                                           Instant syncedAt) {
        Map<String, SupplierRoomType> mappingByExternalRoom = roomTypeMappings.stream()
                .collect(Collectors.toMap(
                        SupplierRoomType::supplierRoomCode,
                        Function.identity(),
                        (first, ignored) -> first));

        Map<RoomTypeId, RatePlanId> ratePlanByRoomType = new HashMap<>();
        for (RatePlan rp : ratePlans.items()) {
            ratePlanByRoomType.putIfAbsent(rp.roomTypeId(), rp.id());
        }

        List<Rate> rates = new ArrayList<>();
        List<Inventory> inventories = new ArrayList<>();
        Map<String, BigDecimal> cacheEntries = new HashMap<>();

        for (SupplierRateData rateData : rateDataList) {
            SupplierRoomType mapping = mappingByExternalRoom.get(rateData.externalRoomId());
            if (mapping == null) {
                continue;
            }

            RatePlanId ratePlanId = ratePlanByRoomType.get(mapping.roomTypeId());
            if (ratePlanId == null) {
                continue;
            }

            for (SupplierRateData.SupplierDailyRate dailyRate : rateData.dailyRates()) {
                rates.add(Rate.forNew(ratePlanId, dailyRate.date(), dailyRate.price(), syncedAt));
                inventories.add(Inventory.forNew(
                        mapping.roomTypeId(), dailyRate.date(), dailyRate.availableCount(), syncedAt));
                cacheEntries.put(ratePlanId.value() + ":" + dailyRate.date(), dailyRate.price());
            }
        }

        return new SupplierRateSyncBundle(rates, inventories, cacheEntries);
    }
}

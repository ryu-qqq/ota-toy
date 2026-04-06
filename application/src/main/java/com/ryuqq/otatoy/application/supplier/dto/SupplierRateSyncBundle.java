package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Rate/Inventory 동기화에 필요한 데이터 묶음.
 * SupplierRateSyncAssembler가 생성하고 Facade가 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierRateSyncBundle(
        List<Rate> rates,
        List<Inventory> inventories,
        Map<String, BigDecimal> cacheEntries
) {

    public boolean isEmpty() {
        return rates.isEmpty() && inventories.isEmpty();
    }
}

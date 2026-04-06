package com.ryuqq.otatoy.application.supplier.facade;

import com.ryuqq.otatoy.application.pricing.port.out.RateCachePort;
import com.ryuqq.otatoy.application.pricing.port.out.RateCommandPort;
import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 공급자 요금/재고 동기화 저장 Facade.
 * 받아서 저장만 한다 — 매핑 조회/도메인 생성은 Processor가 담당.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRateSyncPersistenceFacade {

    private final RateCommandPort rateCommandPort;
    private final InventoryCommandPort inventoryCommandPort;
    private final RateCachePort rateCachePort;

    public SupplierRateSyncPersistenceFacade(RateCommandPort rateCommandPort,
                                              InventoryCommandPort inventoryCommandPort,
                                              RateCachePort rateCachePort) {
        this.rateCommandPort = rateCommandPort;
        this.inventoryCommandPort = inventoryCommandPort;
        this.rateCachePort = rateCachePort;
    }

    /**
     * Rate + Inventory DB 저장 + Redis 캐시 Write-Through를 하나의 트랜잭션으로 묶는다.
     */
    @Transactional
    public void persist(List<Rate> rates, List<Inventory> inventories, Map<String, BigDecimal> cacheEntries) {
        if (!rates.isEmpty()) {
            rateCommandPort.persistAll(rates);
        }
        if (!inventories.isEmpty()) {
            inventoryCommandPort.persistAll(inventories);
        }
        if (!cacheEntries.isEmpty()) {
            rateCachePort.multiSet(cacheEntries);
        }
    }
}

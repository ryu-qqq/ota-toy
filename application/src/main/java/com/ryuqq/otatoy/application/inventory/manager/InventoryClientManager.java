package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.redis.InventoryRedisPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Inventory Redis 접근 ClientManager.
 * 외부 시스템(Redis)과 통신하므로 @Transactional을 선언하지 않는다 (ClientManager 패턴).
 * Redis 장애 시 DB 폴백으로 재고 차감을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryClientManager {

    private final InventoryRedisPort inventoryRedisPort;
    private final InventoryCommandManager inventoryCommandManager;

    public InventoryClientManager(InventoryRedisPort inventoryRedisPort,
                                   InventoryCommandManager inventoryCommandManager) {
        this.inventoryRedisPort = inventoryRedisPort;
        this.inventoryCommandManager = inventoryCommandManager;
    }

    /**
     * 재고 차감. Redis 원자적 DECR 시도 → 장애 시 DB 낙관적 락 폴백.
     */
    public void decrementStock(RoomTypeId roomTypeId, List<LocalDate> dates) {
        try {
            inventoryRedisPort.decrementStock(roomTypeId, dates);
        } catch (Exception e) {
            if (isRedisConnectionFailure(e)) {
                // Redis 장애 — DB 폴백
                inventoryCommandManager.decrementAvailable(roomTypeId, dates);
            } else {
                throw e;
            }
        }
    }

    /**
     * 재고 복구. 예약 실패 시 보상 용도.
     */
    public void incrementStock(RoomTypeId roomTypeId, List<LocalDate> dates) {
        try {
            inventoryRedisPort.incrementStock(roomTypeId, dates);
        } catch (Exception e) {
            // Redis 재고 복구 실패 — 수동 확인 필요
        }
    }

    /**
     * Redis 재고 카운터 초기화. 파트너가 재고를 설정할 때 호출한다.
     */
    public void initializeStock(RoomTypeId roomTypeId, Map<LocalDate, Integer> dateStockMap) {
        try {
            inventoryRedisPort.initializeStock(roomTypeId, dateStockMap);
        } catch (Exception e) {
            // Redis 재고 초기화 실패 — 예약 시 DB 폴백으로 처리됨
        }
    }

    private boolean isRedisConnectionFailure(Exception e) {
        String name = e.getClass().getSimpleName();
        return name.contains("Redis") || name.contains("Connection") || name.contains("Timeout");
    }
}

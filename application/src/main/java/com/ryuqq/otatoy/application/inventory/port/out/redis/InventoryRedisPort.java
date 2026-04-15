package com.ryuqq.otatoy.application.inventory.port.out.redis;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Inventory Redis 전용 Outbound Port (Client Port).
 * Redis 원자적 카운터로 재고를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface InventoryRedisPort {

    /**
     * 원자적 재고 차감 — 날짜별 DECR.
     * 하나라도 0 미만이면 전부 복구(INCR) 후 InventoryExhaustedException 발생.
     */
    void decrementStock(RoomTypeId roomTypeId, List<LocalDate> dates);

    /**
     * 재고 복구 — 날짜별 INCR.
     */
    void incrementStock(RoomTypeId roomTypeId, List<LocalDate> dates);

    /**
     * 재고 카운터 초기화 — 날짜별 SET.
     * 파트너가 재고를 설정할 때 호출한다.
     */
    void initializeStock(RoomTypeId roomTypeId, Map<LocalDate, Integer> dateStockMap);
}

package com.ryuqq.otatoy.application.inventory.port.out;

import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * Inventory 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface InventoryQueryPort {

    /**
     * 특정 객실 유형들의 날짜 범위에 해당하는 재고 목록을 조회한다.
     *
     * @param roomTypeIds 객실 유형 ID 목록
     * @param startDate 시작 날짜 (포함)
     * @param endDate 종료 날짜 (미포함)
     * @return 날짜별 재고 목록
     */
    List<Inventory> findByRoomTypeIdsAndDateRange(List<RoomTypeId> roomTypeIds, LocalDate startDate, LocalDate endDate);
}

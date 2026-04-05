package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryQueryPort;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Inventory 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryReadManager {

    private final InventoryQueryPort inventoryQueryPort;

    public InventoryReadManager(InventoryQueryPort inventoryQueryPort) {
        this.inventoryQueryPort = inventoryQueryPort;
    }

    /**
     * 특정 객실 유형들의 날짜 범위에 해당하는 재고 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<Inventory> findByRoomTypeIdsAndDateRange(List<RoomTypeId> roomTypeIds,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
        return inventoryQueryPort.findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate);
    }
}

package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory 쓰기 트랜잭션 경계 관리자.
 * DB 원자적 UPDATE (WHERE available_count >= 1)로 재고를 차감한다 (ADR-001).
 * Redis 장애 시 DB 폴백으로 사용되며, 정상 흐름에서도 최종 정합성을 보장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryCommandManager {

    private final InventoryCommandPort inventoryCommandPort;

    public InventoryCommandManager(InventoryCommandPort inventoryCommandPort) {
        this.inventoryCommandPort = inventoryCommandPort;
    }

    /**
     * 다중 날짜 재고를 원자적으로 차감한다.
     * 하나라도 실패(available_count < 1)하면 이미 차감한 날짜를 복구하고 예외 발생.
     */
    @Transactional
    public void decrementAvailable(RoomTypeId roomTypeId, List<LocalDate> dates) {
        List<LocalDate> decremented = new ArrayList<>();

        for (LocalDate date : dates) {
            boolean success = inventoryCommandPort.decrementAvailable(roomTypeId.value(), date);
            if (!success) {
                // 이미 차감한 날짜들 복구
                for (LocalDate d : decremented) {
                    inventoryCommandPort.incrementAvailable(roomTypeId.value(), d);
                }
                throw new InventoryExhaustedException();
            }
            decremented.add(date);
        }
    }

    /**
     * 다중 날짜 재고를 복구한다.
     * 예약 취소 시 사용.
     */
    @Transactional
    public void incrementAvailable(RoomTypeId roomTypeId, List<LocalDate> dates) {
        for (LocalDate date : dates) {
            inventoryCommandPort.incrementAvailable(roomTypeId.value(), date);
        }
    }
}

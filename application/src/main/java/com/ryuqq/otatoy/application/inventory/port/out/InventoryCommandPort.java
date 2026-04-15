package com.ryuqq.otatoy.application.inventory.port.out;

import com.ryuqq.otatoy.domain.inventory.Inventory;

import java.time.LocalDate;
import java.util.List;

/**
 * Inventory 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface InventoryCommandPort {

    void persistAll(List<Inventory> inventories);

    /**
     * 원자적 재고 차감 (DB 제약 조건 방식, ADR-001).
     * UPDATE ... WHERE available_count >= 1
     *
     * @return true: 성공, false: 재고 부족
     */
    boolean decrementAvailable(Long roomTypeId, LocalDate date);

    /**
     * 원자적 재고 복구.
     */
    void incrementAvailable(Long roomTypeId, LocalDate date);
}

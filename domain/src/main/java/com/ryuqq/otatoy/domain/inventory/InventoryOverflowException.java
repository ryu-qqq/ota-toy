package com.ryuqq.otatoy.domain.inventory;

/**
 * 재고 복구 시 전체 수량(totalInventory)을 초과할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class InventoryOverflowException extends InventoryException {

    public InventoryOverflowException() {
        super(InventoryErrorCode.INVENTORY_OVERFLOW);
    }
}

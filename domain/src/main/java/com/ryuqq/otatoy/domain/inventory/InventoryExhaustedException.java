package com.ryuqq.otatoy.domain.inventory;

/**
 * 재고가 모두 소진되었을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class InventoryExhaustedException extends InventoryException {

    public InventoryExhaustedException() {
        super(InventoryErrorCode.INVENTORY_EXHAUSTED);
    }
}

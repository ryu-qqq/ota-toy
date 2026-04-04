package com.ryuqq.otatoy.domain.inventory;

/**
 * 재고를 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class InventoryNotFoundException extends InventoryException {

    public InventoryNotFoundException() {
        super(InventoryErrorCode.INVENTORY_NOT_FOUND);
    }
}

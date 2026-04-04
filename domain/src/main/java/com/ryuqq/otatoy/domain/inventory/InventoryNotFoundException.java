package com.ryuqq.otatoy.domain.inventory;

/**
 * 재고를 찾을 수 없을 때 발생하는 예외.
 */
public class InventoryNotFoundException extends InventoryException {

    public InventoryNotFoundException() {
        super(InventoryErrorCode.INVENTORY_NOT_FOUND);
    }
}

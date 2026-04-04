package com.ryuqq.otatoy.domain.inventory;

public class InventoryNotFoundException extends InventoryException {

    public InventoryNotFoundException() {
        super(InventoryErrorCode.INVENTORY_NOT_FOUND);
    }
}

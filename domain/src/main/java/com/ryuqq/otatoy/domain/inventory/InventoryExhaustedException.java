package com.ryuqq.otatoy.domain.inventory;

public class InventoryExhaustedException extends InventoryException {

    public InventoryExhaustedException() {
        super(InventoryErrorCode.INVENTORY_EXHAUSTED);
    }
}

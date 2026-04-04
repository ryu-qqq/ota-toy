package com.ryuqq.otatoy.domain.inventory;

public class InventoryStopSellException extends InventoryException {

    public InventoryStopSellException() {
        super(InventoryErrorCode.INVENTORY_STOP_SELL);
    }
}

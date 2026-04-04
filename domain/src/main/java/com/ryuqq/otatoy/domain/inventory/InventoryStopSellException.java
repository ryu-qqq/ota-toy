package com.ryuqq.otatoy.domain.inventory;

/**
 * 판매가 중지된 재고에 접근할 때 발생하는 예외.
 */
public class InventoryStopSellException extends InventoryException {

    public InventoryStopSellException() {
        super(InventoryErrorCode.INVENTORY_STOP_SELL);
    }
}

package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 재고(Inventory) 도메인의 에러 코드.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND("INV-001", "재고를 찾을 수 없습니다"),
    INVENTORY_EXHAUSTED("INV-002", "재고가 소진되었습니다"),
    INVENTORY_STOP_SELL("INV-003", "판매가 중지된 재고입니다");

    private final String code;
    private final String message;

    InventoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}

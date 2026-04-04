package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND("INV-001", 404, "재고를 찾을 수 없습니다"),
    INVENTORY_EXHAUSTED("INV-002", 409, "재고가 소진되었습니다"),
    INVENTORY_STOP_SELL("INV-003", 409, "판매가 중지된 재고입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    InventoryErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public int getHttpStatus() { return httpStatus; }

    @Override
    public String getMessage() { return message; }
}

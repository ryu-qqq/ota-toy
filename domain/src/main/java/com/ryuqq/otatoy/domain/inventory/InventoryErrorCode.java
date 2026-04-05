package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND("INV-001", "재고를 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
    INVENTORY_EXHAUSTED("INV-002", "재고가 소진되었습니다", ErrorCategory.CONFLICT),
    INVENTORY_STOP_SELL("INV-003", "판매가 중지된 재고입니다", ErrorCategory.FORBIDDEN),
    INVENTORY_OVERFLOW("INV-004", "복구 수량이 전체 수량을 초과합니다", ErrorCategory.VALIDATION);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    InventoryErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public ErrorCategory getCategory() { return category; }
}

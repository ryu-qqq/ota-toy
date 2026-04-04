package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 공급자(Supplier) 도메인의 에러 코드.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum SupplierErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND("SUP-001", "공급자를 찾을 수 없습니다"),
    SUPPLIER_ALREADY_SUSPENDED("SUP-002", "이미 정지된 공급자입니다"),
    SUPPLIER_SYNC_FAILED("SUP-003", "공급자 동기화에 실패했습니다"),
    SUPPLIER_ALREADY_TERMINATED("SUP-004", "이미 해지된 공급자입니다"),
    SUPPLIER_ALREADY_ACTIVE("SUP-005", "이미 활성 상태인 공급자입니다"),
    INVALID_SUPPLIER_STATE_TRANSITION("SUP-006", "허용되지 않는 공급자 상태 전이입니다");

    private final String code;
    private final String message;

    SupplierErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}

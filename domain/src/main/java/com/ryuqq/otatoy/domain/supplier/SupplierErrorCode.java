package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum SupplierErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND("SUP-001", 404, "공급자를 찾을 수 없습니다"),
    SUPPLIER_ALREADY_SUSPENDED("SUP-002", 409, "이미 정지된 공급자입니다"),
    SUPPLIER_SYNC_FAILED("SUP-003", 500, "공급자 동기화에 실패했습니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    SupplierErrorCode(String code, int httpStatus, String message) {
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

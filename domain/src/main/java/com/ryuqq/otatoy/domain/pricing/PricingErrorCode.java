package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum PricingErrorCode implements ErrorCode {

    RATE_PLAN_NOT_FOUND("PRC-001", 404, "요금 정책을 찾을 수 없습니다"),
    RATE_RULE_NOT_FOUND("PRC-002", 404, "요금 규칙을 찾을 수 없습니다"),
    INVALID_RATE_RULE_PERIOD("PRC-003", 400, "유효하지 않은 요금 규칙 기간입니다"),
    RATE_NOT_AVAILABLE("PRC-004", 404, "해당 날짜에 요금이 설정되지 않았습니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    PricingErrorCode(String code, int httpStatus, String message) {
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

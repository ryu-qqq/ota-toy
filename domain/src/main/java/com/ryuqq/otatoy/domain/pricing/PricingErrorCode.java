package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 요금(Pricing) 도메인의 에러 코드.
 */
public enum PricingErrorCode implements ErrorCode {

    RATE_PLAN_NOT_FOUND("PRC-001", "요금 정책을 찾을 수 없습니다"),
    RATE_RULE_NOT_FOUND("PRC-002", "요금 규칙을 찾을 수 없습니다"),
    INVALID_RATE_RULE_PERIOD("PRC-003", "유효하지 않은 요금 규칙 기간입니다"),
    RATE_NOT_AVAILABLE("PRC-004", "해당 날짜에 요금이 설정되지 않았습니다");

    private final String code;
    private final String message;

    PricingErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}

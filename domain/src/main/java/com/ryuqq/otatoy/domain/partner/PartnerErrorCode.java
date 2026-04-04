package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum PartnerErrorCode implements ErrorCode {

    PARTNER_NOT_FOUND("PTN-001", 404, "파트너를 찾을 수 없습니다"),
    PARTNER_ALREADY_SUSPENDED("PTN-002", 409, "이미 정지된 파트너입니다"),
    PARTNER_ALREADY_ACTIVE("PTN-003", 409, "이미 활성 상태인 파트너입니다"),
    PARTNER_MEMBER_NOT_FOUND("PTN-004", 404, "파트너 멤버를 찾을 수 없습니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    PartnerErrorCode(String code, int httpStatus, String message) {
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

package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum AccommodationErrorCode implements ErrorCode {

    PROPERTY_NOT_FOUND("ACC-001", 404, "숙소를 찾을 수 없습니다"),
    ROOM_TYPE_NOT_FOUND("ACC-002", 404, "객실 유형을 찾을 수 없습니다"),
    RATE_PLAN_NOT_FOUND("ACC-003", 404, "요금 정책을 찾을 수 없습니다"),
    INVALID_PROPERTY_STATUS("ACC-004", 400, "유효하지 않은 숙소 상태입니다"),
    INVALID_ROOM_TYPE("ACC-005", 400, "유효하지 않은 객실 정보입니다"),
    INVALID_RATE_RULE("ACC-006", 400, "유효하지 않은 요금 규칙입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    AccommodationErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 숙박(Accommodation) 도메인의 에러 코드.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum AccommodationErrorCode implements ErrorCode {

    PROPERTY_NOT_FOUND("ACC-001", "숙소를 찾을 수 없습니다"),
    ROOM_TYPE_NOT_FOUND("ACC-002", "객실 유형을 찾을 수 없습니다"),
    INVALID_PROPERTY_STATUS("ACC-004", "유효하지 않은 숙소 상태입니다"),
    INVALID_ROOM_TYPE("ACC-005", "유효하지 않은 객실 정보입니다");

    private final String code;
    private final String message;

    AccommodationErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}

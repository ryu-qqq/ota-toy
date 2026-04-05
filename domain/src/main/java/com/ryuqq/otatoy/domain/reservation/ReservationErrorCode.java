package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {

    RESERVATION_NOT_FOUND("RSV-001", "예약을 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
    INVALID_RESERVATION_STATE("RSV-002", "유효하지 않은 예약 상태 전이입니다", ErrorCategory.VALIDATION),
    RESERVATION_ALREADY_CANCELLED("RSV-003", "이미 취소된 예약입니다", ErrorCategory.CONFLICT),
    RESERVATION_ALREADY_COMPLETED("RSV-004", "이미 완료된 예약입니다", ErrorCategory.CONFLICT);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    ReservationErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public ErrorCategory getCategory() { return category; }
}

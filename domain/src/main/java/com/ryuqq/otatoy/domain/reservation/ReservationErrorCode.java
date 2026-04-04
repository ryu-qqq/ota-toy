package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 예약(Reservation) 도메인의 에러 코드.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum ReservationErrorCode implements ErrorCode {

    RESERVATION_NOT_FOUND("RSV-001", "예약을 찾을 수 없습니다"),
    INVALID_RESERVATION_STATE("RSV-002", "유효하지 않은 예약 상태 전이입니다"),
    RESERVATION_ALREADY_CANCELLED("RSV-003", "이미 취소된 예약입니다"),
    RESERVATION_ALREADY_COMPLETED("RSV-004", "이미 완료된 예약입니다");

    private final String code;
    private final String message;

    ReservationErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}

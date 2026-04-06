package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 세션이 PENDING 상태가 아닌데 확정/만료를 시도할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class InvalidSessionStateException extends ReservationException {

    public InvalidSessionStateException() {
        super(ReservationErrorCode.INVALID_SESSION_STATE);
    }
}

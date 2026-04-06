package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 세션이 만료되었을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class ReservationSessionExpiredException extends ReservationException {

    public ReservationSessionExpiredException() {
        super(ReservationErrorCode.RESERVATION_SESSION_EXPIRED);
    }
}

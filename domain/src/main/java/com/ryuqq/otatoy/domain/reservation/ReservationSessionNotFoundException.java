package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 세션을 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class ReservationSessionNotFoundException extends ReservationException {

    public ReservationSessionNotFoundException() {
        super(ReservationErrorCode.RESERVATION_SESSION_NOT_FOUND);
    }
}

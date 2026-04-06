package com.ryuqq.otatoy.domain.reservation;

/**
 * 존재하지 않는 예약을 조회하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class ReservationNotFoundException extends ReservationException {

    public ReservationNotFoundException() {
        super(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }
}

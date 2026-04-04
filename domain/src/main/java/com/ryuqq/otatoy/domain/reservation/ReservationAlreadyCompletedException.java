package com.ryuqq.otatoy.domain.reservation;

/**
 * 이미 완료된 예약을 변경하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class ReservationAlreadyCompletedException extends ReservationException {

    public ReservationAlreadyCompletedException() {
        super(ReservationErrorCode.RESERVATION_ALREADY_COMPLETED);
    }
}

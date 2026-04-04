package com.ryuqq.otatoy.domain.reservation;

/**
 * 이미 취소된 예약을 다시 취소하려 할 때 발생하는 예외.
 */
public class ReservationAlreadyCancelledException extends ReservationException {

    public ReservationAlreadyCancelledException() {
        super(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);
    }
}

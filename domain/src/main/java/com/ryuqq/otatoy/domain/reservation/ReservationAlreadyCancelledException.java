package com.ryuqq.otatoy.domain.reservation;

public class ReservationAlreadyCancelledException extends ReservationException {

    public ReservationAlreadyCancelledException() {
        super(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);
    }
}

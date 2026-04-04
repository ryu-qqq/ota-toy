package com.ryuqq.otatoy.domain.reservation;

public class ReservationAlreadyCompletedException extends ReservationException {

    public ReservationAlreadyCompletedException() {
        super(ReservationErrorCode.RESERVATION_ALREADY_COMPLETED);
    }
}

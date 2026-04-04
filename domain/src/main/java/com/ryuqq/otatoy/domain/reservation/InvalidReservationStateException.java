package com.ryuqq.otatoy.domain.reservation;

public class InvalidReservationStateException extends ReservationException {

    public InvalidReservationStateException() {
        super(ReservationErrorCode.INVALID_RESERVATION_STATE);
    }
}

package com.ryuqq.otatoy.domain.reservation;

/**
 * 허용되지 않는 예약 상태 전이를 시도할 때 발생하는 예외.
 */
public class InvalidReservationStateException extends ReservationException {

    public InvalidReservationStateException() {
        super(ReservationErrorCode.INVALID_RESERVATION_STATE);
    }
}

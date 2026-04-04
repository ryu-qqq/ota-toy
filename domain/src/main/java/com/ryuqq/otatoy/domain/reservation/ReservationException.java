package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.DomainException;

public class ReservationException extends DomainException {

    protected ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }
}

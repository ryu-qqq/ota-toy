package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 예약 도메인의 기본 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class ReservationException extends DomainException {

    protected ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }
}

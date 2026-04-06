package com.ryuqq.otatoy.application.reservation.manager;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationQueryPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.reservation.ReservationNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reservation 읽기 트랜잭션 경계 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationReadManager {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationReadManager(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    /**
     * ID로 예약을 조회한다. 없으면 예외 발생.
     */
    @Transactional(readOnly = true)
    public Reservation getById(ReservationId id) {
        return reservationQueryPort.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }
}

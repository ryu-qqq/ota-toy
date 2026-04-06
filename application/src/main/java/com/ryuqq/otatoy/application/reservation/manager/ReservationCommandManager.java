package com.ryuqq.otatoy.application.reservation.manager;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reservation 쓰기 트랜잭션 경계 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationCommandManager {

    private final ReservationCommandPort reservationCommandPort;

    public ReservationCommandManager(ReservationCommandPort reservationCommandPort) {
        this.reservationCommandPort = reservationCommandPort;
    }

    /**
     * 예약(Aggregate Root)을 저장한다.
     */
    @Transactional
    public Long persist(Reservation reservation) {
        return reservationCommandPort.persist(reservation);
    }
}

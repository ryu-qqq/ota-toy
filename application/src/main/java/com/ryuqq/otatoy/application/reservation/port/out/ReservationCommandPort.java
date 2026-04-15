package com.ryuqq.otatoy.application.reservation.port.out;

import com.ryuqq.otatoy.domain.reservation.Reservation;

/**
 * Reservation 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationCommandPort {

    /**
     * 예약(Aggregate Root)을 저장한다.
     * Reservation + ReservationLine + ReservationItem을 원자적으로 저장한다.
     *
     * @param reservation 저장할 예약 도메인 객체
     * @return 저장된 예약 ID
     */
    Long persist(Reservation reservation);
}

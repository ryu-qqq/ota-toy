package com.ryuqq.otatoy.application.reservation.port.out;

import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationId;

import java.util.Optional;

/**
 * Reservation 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-002).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationQueryPort {

    /**
     * ID로 예약을 조회한다.
     *
     * @param id 예약 ID
     * @return 예약 (없으면 empty)
     */
    Optional<Reservation> findById(ReservationId id);
}

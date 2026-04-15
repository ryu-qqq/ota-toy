package com.ryuqq.otatoy.application.reservation.port.out;

import com.ryuqq.otatoy.domain.reservation.ReservationSession;

/**
 * ReservationSession 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationSessionCommandPort {

    /**
     * 예약 세션을 저장/갱신한다.
     *
     * @param session 저장할 예약 세션 도메인 객체
     * @return 저장된 세션 ID
     */
    Long persist(ReservationSession session);
}

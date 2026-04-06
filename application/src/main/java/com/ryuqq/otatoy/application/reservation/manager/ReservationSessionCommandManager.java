package com.ryuqq.otatoy.application.reservation.manager;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionCommandPort;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReservationSession 쓰기 트랜잭션 경계 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionCommandManager {

    private final ReservationSessionCommandPort sessionCommandPort;

    public ReservationSessionCommandManager(ReservationSessionCommandPort sessionCommandPort) {
        this.sessionCommandPort = sessionCommandPort;
    }

    /**
     * 예약 세션을 저장/갱신한다.
     */
    @Transactional
    public Long persist(ReservationSession session) {
        return sessionCommandPort.persist(session);
    }
}

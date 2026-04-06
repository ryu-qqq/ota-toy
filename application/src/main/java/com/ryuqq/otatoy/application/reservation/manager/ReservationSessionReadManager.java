package com.ryuqq.otatoy.application.reservation.manager;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionQueryPort;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ReservationSession 읽기 트랜잭션 경계 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionReadManager {

    private final ReservationSessionQueryPort sessionQueryPort;

    public ReservationSessionReadManager(ReservationSessionQueryPort sessionQueryPort) {
        this.sessionQueryPort = sessionQueryPort;
    }

    /**
     * ID로 예약 세션을 조회한다. 없으면 예외 발생.
     */
    @Transactional(readOnly = true)
    public ReservationSession getById(Long id) {
        return sessionQueryPort.findById(id)
                .orElseThrow(ReservationSessionNotFoundException::new);
    }

    /**
     * 예약 ID로 예약 세션을 조회한다. 없으면 예외 발생.
     * 예약 취소 시 roomTypeId를 추출하기 위해 사용한다.
     */
    @Transactional(readOnly = true)
    public ReservationSession getByReservationId(Long reservationId) {
        return sessionQueryPort.findByReservationId(reservationId)
                .orElseThrow(ReservationSessionNotFoundException::new);
    }

    /**
     * 멱등키로 예약 세션을 조회한다. 없으면 empty.
     */
    @Transactional(readOnly = true)
    public Optional<ReservationSession> findByIdempotencyKey(String idempotencyKey) {
        return sessionQueryPort.findByIdempotencyKey(idempotencyKey);
    }
}

package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;
import com.ryuqq.otatoy.application.reservation.facade.ReservationPersistenceFacade;
import com.ryuqq.otatoy.application.reservation.factory.ReservationFactory;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.application.reservation.port.in.ConfirmReservationUseCase;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionStatus;

import org.springframework.stereotype.Service;

/**
 * 예약 확정 Service (2단계: 세션 → 예약 변환).
 * @Transactional 금지 -- 트랜잭션 경계는 PersistenceFacade에서 관리한다.
 *
 * 흐름:
 * 1. 세션 조회 + 유효성 확인 (PENDING + 미만료)
 * 2. 예약 생성 (Factory)
 * 3. DB 저장 (Reservation + 재고 차감 + 세션 상태 CONFIRMED)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class ConfirmReservationService implements ConfirmReservationUseCase {

    private final ReservationSessionReadManager sessionReadManager;
    private final ReservationFactory reservationFactory;
    private final ReservationPersistenceFacade reservationPersistenceFacade;

    public ConfirmReservationService(ReservationSessionReadManager sessionReadManager,
                                      ReservationFactory reservationFactory,
                                      ReservationPersistenceFacade reservationPersistenceFacade) {
        this.sessionReadManager = sessionReadManager;
        this.reservationFactory = reservationFactory;
        this.reservationPersistenceFacade = reservationPersistenceFacade;
    }

    @Override
    public Long execute(ConfirmReservationCommand command) {
        // 1. 세션 조회 + 유효성 확인 (PENDING + 미만료)
        ReservationSession session = sessionReadManager.getById(command.sessionId());

        // 1-1. 이미 확정된 세션이면 기존 reservationId 반환 (멱등)
        if (session.status() == ReservationSessionStatus.CONFIRMED) {
            return session.reservationId();
        }

        // 2. 예약 생성
        Reservation reservation = reservationFactory.createFromSession(session, command);

        // 3. DB 저장 (Reservation + 재고 차감 + 세션 상태 CONFIRMED)
        return reservationPersistenceFacade.confirmReservation(reservation, session);
    }
}

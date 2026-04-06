package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CancelReservationCommand;
import com.ryuqq.otatoy.application.reservation.facade.CancelReservationFacade;
import com.ryuqq.otatoy.application.reservation.manager.ReservationReadManager;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.application.reservation.port.in.CancelReservationUseCase;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 취소 Service.
 * @Transactional 금지 -- 트랜잭션 경계는 CancelReservationFacade에서 관리한다.
 *
 * 흐름:
 * 1. 예약 조회 (ReservationReadManager)
 * 2. 예약 세션 조회 — roomTypeId + stayDates 추출 (ReservationSessionReadManager)
 * 3. DB 취소 + DB 재고 복구 (CancelReservationFacade — 같은 트랜잭션)
 * 4. Redis 재고 복구 (InventoryClientManager — DB 커밋 후)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationReadManager reservationReadManager;
    private final ReservationSessionReadManager sessionReadManager;
    private final CancelReservationFacade cancelReservationFacade;
    private final InventoryClientManager inventoryClientManager;

    public CancelReservationService(ReservationReadManager reservationReadManager,
                                     ReservationSessionReadManager sessionReadManager,
                                     CancelReservationFacade cancelReservationFacade,
                                     InventoryClientManager inventoryClientManager) {
        this.reservationReadManager = reservationReadManager;
        this.sessionReadManager = sessionReadManager;
        this.cancelReservationFacade = cancelReservationFacade;
        this.inventoryClientManager = inventoryClientManager;
    }

    @Override
    public void execute(CancelReservationCommand command) {
        // 1. 예약 조회
        Reservation reservation = reservationReadManager.getById(command.reservationId());

        // 2. 예약 세션 조회 — roomTypeId + stayDates 추출
        ReservationSession session = sessionReadManager.getByReservationId(
                reservation.id().value());
        RoomTypeId roomTypeId = session.roomTypeId();
        List<LocalDate> stayDates = session.stayDates();

        // 3. DB 취소 + DB 재고 복구 (같은 트랜잭션)
        cancelReservationFacade.cancel(reservation, command.cancelReason(),
                roomTypeId, stayDates);

        // 4. Redis 재고 복구 (DB 커밋 후, 보상 대상 아님)
        inventoryClientManager.incrementStock(roomTypeId, stayDates);
    }
}

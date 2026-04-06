package com.ryuqq.otatoy.application.reservation.facade;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.inventory.manager.InventoryCommandManager;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 취소 영속화 Facade (APP-FCD-001).
 * Reservation 상태 변경(cancel) + DB 재고 복구를 하나의 트랜잭션에서 원자적으로 수행한다.
 * TimeProvider를 주입받아 cancel() 도메인 메서드에 now를 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class CancelReservationFacade {

    private final ReservationCommandPort reservationCommandPort;
    private final InventoryCommandManager inventoryCommandManager;
    private final TimeProvider timeProvider;

    public CancelReservationFacade(ReservationCommandPort reservationCommandPort,
                                    InventoryCommandManager inventoryCommandManager,
                                    TimeProvider timeProvider) {
        this.reservationCommandPort = reservationCommandPort;
        this.inventoryCommandManager = inventoryCommandManager;
        this.timeProvider = timeProvider;
    }

    /**
     * 예약 취소 + DB 재고 복구를 원자적으로 수행한다.
     * 1) 도메인 cancel() 호출 (이미 취소 상태면 ReservationAlreadyCancelledException)
     * 2) Reservation 저장 (상태 변경 반영)
     * 3) DB 재고 복구 (available_count INCREMENT)
     *
     * @param reservation 취소할 예약 도메인 객체
     * @param cancelReason 취소 사유
     * @param roomTypeId 재고 복구 대상 객실 유형 ID
     * @param stayDates 재고 복구 대상 숙박 날짜 목록
     */
    @Transactional
    public void cancel(Reservation reservation, String cancelReason,
                       RoomTypeId roomTypeId, List<LocalDate> stayDates) {
        // 1. 도메인에서 취소 (이미 취소면 예외)
        reservation.cancel(cancelReason, timeProvider.now());

        // 2. 예약 저장 (상태 변경 반영)
        reservationCommandPort.persist(reservation);

        // 3. DB 재고 복구
        inventoryCommandManager.incrementAvailable(roomTypeId, stayDates);
    }
}

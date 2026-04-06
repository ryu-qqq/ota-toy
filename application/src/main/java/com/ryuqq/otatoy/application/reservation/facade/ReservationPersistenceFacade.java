package com.ryuqq.otatoy.application.reservation.facade;

import com.ryuqq.otatoy.application.inventory.manager.InventoryCommandManager;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Reservation 영속화 Facade (APP-FCD-001, ADR-001).
 * Reservation 저장 + DB 재고 차감을 하나의 트랜잭션에서 원자적으로 수행한다.
 * Redis가 1차 게이트키퍼, DB가 최종 정합성을 보장하는 2중 구조.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationPersistenceFacade {

    private final ReservationCommandPort reservationCommandPort;
    private final InventoryCommandManager inventoryCommandManager;

    public ReservationPersistenceFacade(ReservationCommandPort reservationCommandPort,
                                        InventoryCommandManager inventoryCommandManager) {
        this.reservationCommandPort = reservationCommandPort;
        this.inventoryCommandManager = inventoryCommandManager;
    }

    /**
     * 예약 저장 + DB 재고 차감을 원자적으로 수행한다.
     * DB 재고 차감은 UPDATE ... WHERE available_count >= 1 (원자적 UPDATE).
     * 재고 부족 시 InventoryExhaustedException → 트랜잭션 롤백.
     */
    @Transactional
    public Long persist(Reservation reservation, RoomTypeId roomTypeId, List<LocalDate> stayDates) {
        // 1. DB 재고 원자적 차감 (최종 정합성 보장)
        inventoryCommandManager.decrementAvailable(roomTypeId, stayDates);

        // 2. 예약 저장
        return reservationCommandPort.persist(reservation);
    }
}

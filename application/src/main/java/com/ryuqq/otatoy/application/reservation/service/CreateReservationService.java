package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;
import com.ryuqq.otatoy.application.reservation.facade.ReservationPersistenceFacade;
import com.ryuqq.otatoy.application.reservation.factory.ReservationFactory;
import com.ryuqq.otatoy.application.reservation.port.in.CreateReservationUseCase;
import com.ryuqq.otatoy.application.reservation.validator.CreateReservationValidator;
import com.ryuqq.otatoy.domain.reservation.Reservation;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 생성 Service (APP-SVC-001).
 * @Transactional 금지 — Redis 호출이 DB 트랜잭션에 묶이지 않도록 한다.
 *
 * 흐름:
 * 1. 검증 (숙소/객실 존재 확인)
 * 2. Redis 재고 원자적 차감 (Redis 장애 시 DB 낙관적 락 폴백)
 * 3. 도메인 객체 생성 (Factory)
 * 4. DB 저장 (PersistenceFacade)
 * 5. 3~4 실패 시 Redis 재고 복구 (보상)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CreateReservationService implements CreateReservationUseCase {

    private final CreateReservationValidator validator;
    private final ReservationFactory reservationFactory;
    private final InventoryClientManager inventoryClientManager;
    private final ReservationPersistenceFacade reservationPersistenceFacade;

    public CreateReservationService(CreateReservationValidator validator,
                                     ReservationFactory reservationFactory,
                                     InventoryClientManager inventoryClientManager,
                                     ReservationPersistenceFacade reservationPersistenceFacade) {
        this.validator = validator;
        this.reservationFactory = reservationFactory;
        this.inventoryClientManager = inventoryClientManager;
        this.reservationPersistenceFacade = reservationPersistenceFacade;
    }

    @Override
    public Long execute(CreateReservationCommand command) {
        // 1. 검증
        validator.validate(command);

        // 2. 재고 차감 (Redis DECR → 장애 시 DB 폴백)
        List<LocalDate> stayDates = command.stayDates();
        inventoryClientManager.decrementStock(command.roomTypeId(), stayDates);

        try {
            // 3. 도메인 객체 생성
            Reservation reservation = reservationFactory.create(command);

            // 4. DB 저장 + DB 재고 차감 (같은 트랜잭션 — ADR-001 2중 구조)
            return reservationPersistenceFacade.persist(reservation, command.roomTypeId(), stayDates);
        } catch (Exception e) {
            // 5. 실패 시 재고 복구 (보상)
            inventoryClientManager.incrementStock(command.roomTypeId(), stayDates);
            throw e;
        }
    }
}

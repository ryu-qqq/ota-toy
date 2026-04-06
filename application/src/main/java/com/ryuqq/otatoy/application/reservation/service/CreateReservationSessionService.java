package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.factory.ReservationSessionFactory;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionCommandManager;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.application.reservation.port.in.CreateReservationSessionUseCase;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.application.reservation.validator.ReservationSessionValidator;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 예약 세션 생성 Service (1단계: 재고 선점).
 * @Transactional 금지 -- Redis 호출이 DB 트랜잭션에 묶이지 않도록 한다.
 *
 * 흐름:
 * 0. 멱등키 확인 — 이미 같은 키로 세션이 있으면 기존 sessionId 반환
 * 1. 검증 (숙소/객실 존재 확인)
 * 2. Redis 재고 원자적 차감
 * 3. 도메인 객체 생성 (Factory)
 * 4. DB 저장 (CommandManager)
 * 5. 3~4 실패 시 Redis 재고 복구 (보상)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CreateReservationSessionService implements CreateReservationSessionUseCase {

    private final ReservationSessionValidator validator;
    private final ReservationSessionFactory sessionFactory;
    private final InventoryClientManager inventoryClientManager;
    private final ReservationSessionCommandManager sessionCommandManager;
    private final ReservationSessionReadManager sessionReadManager;

    public CreateReservationSessionService(ReservationSessionValidator validator,
                                            ReservationSessionFactory sessionFactory,
                                            InventoryClientManager inventoryClientManager,
                                            ReservationSessionCommandManager sessionCommandManager,
                                            ReservationSessionReadManager sessionReadManager) {
        this.validator = validator;
        this.sessionFactory = sessionFactory;
        this.inventoryClientManager = inventoryClientManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionReadManager = sessionReadManager;
    }

    @Override
    public ReservationSessionResult execute(CreateReservationSessionCommand command) {
        // 0. 멱등키 확인 — 이미 같은 키로 세션이 있으면 기존 sessionId 반환
        Optional<ReservationSession> existing =
            sessionReadManager.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return ReservationSessionResult.from(existing.get());
        }

        // 1. 검증
        validator.validate(command);

        // 2. 재고 차감 (Redis DECR → 장애 시 DB 폴백)
        List<LocalDate> stayDates = command.stayDates();
        inventoryClientManager.decrementStock(command.roomTypeId(), stayDates);

        try {
            // 3. 도메인 객체 생성
            ReservationSession session = sessionFactory.create(command);

            // 4. DB 저장
            Long sessionId = sessionCommandManager.persist(session);
            return ReservationSessionResult.of(sessionId, session);
        } catch (Exception e) {
            // 5. 실패 시 재고 복구 (보상)
            inventoryClientManager.incrementStock(command.roomTypeId(), stayDates);
            throw e;
        }
    }
}

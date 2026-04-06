package com.ryuqq.otatoy.application.reservation.port.in;

import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;

/**
 * 예약 세션 생성 UseCase (1단계: 재고 선점).
 * Redis 원자적 카운터로 재고를 선점하고, 예약 세션을 생성한다.
 * 세션은 10분 TTL을 가지며, 만료 시 재고가 복구된다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CreateReservationSessionUseCase {

    /**
     * 예약 세션을 생성한다.
     *
     * @param command 세션 생성 요청
     * 
     */
    ReservationSessionResult execute(CreateReservationSessionCommand command);
}

package com.ryuqq.otatoy.application.reservation.port.in;

import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;

/**
 * 예약 생성 UseCase (APP-UC-001).
 * Redis 원자적 카운터로 재고를 차감하고, DB에 예약을 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CreateReservationUseCase {

    /**
     * 예약을 생성한다.
     *
     * @param command 예약 생성 요청
     * @return 생성된 예약 ID
     */
    Long execute(CreateReservationCommand command);
}

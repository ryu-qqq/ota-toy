package com.ryuqq.otatoy.application.reservation.port.in;

import com.ryuqq.otatoy.application.reservation.dto.command.CancelReservationCommand;

/**
 * 예약 취소 UseCase.
 * 예약을 취소하고 Redis + DB 재고를 복구한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CancelReservationUseCase {

    /**
     * 예약을 취소한다.
     *
     * @param command 예약 취소 요청
     */
    void execute(CancelReservationCommand command);
}

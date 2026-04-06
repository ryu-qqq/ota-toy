package com.ryuqq.otatoy.application.reservation.port.in;

import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;

/**
 * 예약 확정 UseCase (2단계: 세션 → 예약 변환).
 * 유효한 세션을 기반으로 예약을 확정하고, DB 재고를 원자적으로 차감한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ConfirmReservationUseCase {

    /**
     * 예약을 확정한다.
     *
     * @param command 예약 확정 요청
     * @return 생성된 예약 ID
     */
    Long execute(ConfirmReservationCommand command);
}

package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.reservation.ReservationId;

/**
 * 예약 취소 Command DTO.
 * reservationId와 취소 사유를 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CancelReservationCommand(
    ReservationId reservationId,
    String cancelReason
) {
}

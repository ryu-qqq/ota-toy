package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.reservation.GuestInfo;

import java.util.List;

/**
 * 예약 확정 Command DTO (2단계: 세션 → 예약 변환).
 * 세션에 이미 숙소/객실/요금 정보가 있으므로, 확정 시에는 세션ID + 고객/투숙객 정보만 필요하다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ConfirmReservationCommand(
    Long sessionId,
    long customerId,
    GuestInfo guestInfo,
    String bookingSnapshot,
    List<CreateReservationLineCommand> lines
) {
}

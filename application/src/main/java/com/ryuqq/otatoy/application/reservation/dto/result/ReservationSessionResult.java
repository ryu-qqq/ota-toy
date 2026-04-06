package com.ryuqq.otatoy.application.reservation.dto.result;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 예약 세션 생성 결과.
 * 프론트에서 결제 페이지에 표시할 정보를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ReservationSessionResult(
    Long sessionId,
    Money totalAmount,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    Instant expiresAt
) {

    public static ReservationSessionResult from(ReservationSession session) {
        return new ReservationSessionResult(
            session.id().value(),
            session.totalAmount(),
            session.checkIn(),
            session.checkOut(),
            session.guestCount(),
            session.createdAt().plusSeconds(600)
        );
    }

    /**
     * persist 직후 — 아직 ID가 도메인 객체에 없을 때 사용.
     */
    public static ReservationSessionResult of(Long sessionId, ReservationSession session) {
        return new ReservationSessionResult(
            sessionId,
            session.totalAmount(),
            session.checkIn(),
            session.checkOut(),
            session.guestCount(),
            session.createdAt().plusSeconds(600)
        );
    }
}

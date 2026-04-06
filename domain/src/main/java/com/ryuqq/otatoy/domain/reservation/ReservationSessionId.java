package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 세션 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ReservationSessionId(Long value) {

    public static ReservationSessionId of(Long value) {
        return new ReservationSessionId(value);
    }

    public static ReservationSessionId forNew() { return new ReservationSessionId(null); }

    public boolean isNew() {
        return value == null;
    }
}

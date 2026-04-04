package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 항목 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record ReservationItemId(Long value) {

    public static ReservationItemId of(Long value) {
        return new ReservationItemId(value);
    }

    public static ReservationItemId forNew() { return new ReservationItemId(null); }

    public boolean isNew() {
        return value == null;
    }
}

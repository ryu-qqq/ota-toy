package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record ReservationId(Long value) {

    public static ReservationId of(Long value) {
        return new ReservationId(value);
    }

    public static ReservationId forNew() { return new ReservationId(null); }

    public boolean isNew() {
        return value == null;
    }
}

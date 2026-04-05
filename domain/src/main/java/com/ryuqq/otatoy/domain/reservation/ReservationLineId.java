package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 라인 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record ReservationLineId(Long value) {

    public static ReservationLineId of(Long value) {
        return new ReservationLineId(value);
    }

    public static ReservationLineId forNew() {
        return new ReservationLineId(null);
    }

    public boolean isNew() {
        return value == null;
    }
}

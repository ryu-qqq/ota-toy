package com.ryuqq.otatoy.domain.reservation;

public record ReservationId(Long value) {

    public static ReservationId of(Long value) {
        return new ReservationId(value);
    }

    public static ReservationId forNew() { return new ReservationId(null); }

    public boolean isNew() {
        return value == null;
    }
}

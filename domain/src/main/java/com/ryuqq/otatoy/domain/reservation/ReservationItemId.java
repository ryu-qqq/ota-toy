package com.ryuqq.otatoy.domain.reservation;

public record ReservationItemId(Long value) {

    public static ReservationItemId of(Long value) {
        return new ReservationItemId(value);
    }

    public static ReservationItemId forNew() { return new ReservationItemId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.reservation;

public record ReservationItemId(Long value) {

    public static ReservationItemId of(Long value) {
        return new ReservationItemId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

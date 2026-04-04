package com.ryuqq.otatoy.domain.reservation;

public record ReservationId(Long value) {

    public static ReservationId of(Long value) {
        return new ReservationId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

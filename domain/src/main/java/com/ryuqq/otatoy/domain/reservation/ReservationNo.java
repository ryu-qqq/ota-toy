package com.ryuqq.otatoy.domain.reservation;

public record ReservationNo(String value) {

    public ReservationNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("예약 번호는 필수입니다");
        }
    }

    public static ReservationNo of(String value) {
        return new ReservationNo(value);
    }
}

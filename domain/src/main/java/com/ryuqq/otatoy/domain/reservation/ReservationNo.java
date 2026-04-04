package com.ryuqq.otatoy.domain.reservation;

public record ReservationNo(String value) {

    private static final int MAX_LENGTH = 50;

    public ReservationNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("예약 번호는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("예약 번호는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static ReservationNo of(String value) {
        return new ReservationNo(value);
    }
}

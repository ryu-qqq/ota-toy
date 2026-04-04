package com.ryuqq.otatoy.domain.reservation;

public enum ReservationStatus {

    PENDING("대기"),
    CONFIRMED("확정"),
    CANCELLED("취소"),
    COMPLETED("완료");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

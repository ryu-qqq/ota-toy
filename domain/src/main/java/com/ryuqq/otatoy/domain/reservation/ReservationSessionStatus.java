package com.ryuqq.otatoy.domain.reservation;

/**
 * 예약 세션 상태.
 * PENDING: 재고 선점 중 (TTL 10분), CONFIRMED: 예약 확정, EXPIRED: 만료.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum ReservationSessionStatus {

    PENDING("대기"),
    CONFIRMED("확정"),
    EXPIRED("만료");

    private final String displayName;

    ReservationSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

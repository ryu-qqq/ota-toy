package com.ryuqq.otatoy.domain.reservation;

import java.util.Map;
import java.util.Set;

/**
 * 예약 상태.
 * PENDING: 대기, CONFIRMED: 확정, CANCELLED: 취소, COMPLETED: 완료, NO_SHOW: 노쇼.
 * 상태 전이 규칙을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum ReservationStatus {

    PENDING("대기"),
    CONFIRMED("확정"),
    CANCELLED("취소"),
    COMPLETED("완료"),
    NO_SHOW("노쇼");

    private static final Map<ReservationStatus, Set<ReservationStatus>> TRANSITIONS = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(COMPLETED, NO_SHOW, CANCELLED)
    );

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitTo(ReservationStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public ReservationStatus transitTo(ReservationStatus target) {
        if (!canTransitTo(target)) {
            throw new InvalidReservationStateException();
        }
        return target;
    }

    public boolean isTerminal() {
        return !TRANSITIONS.containsKey(this);
    }

    public String displayName() {
        return displayName;
    }
}

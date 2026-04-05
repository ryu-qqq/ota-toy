package com.ryuqq.otatoy.domain.member;

import java.util.Map;
import java.util.Set;

/**
 * 회원 상태.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public enum MemberStatus {

    ACTIVE("활성"),
    SUSPENDED("정지");

    private static final Map<MemberStatus, Set<MemberStatus>> TRANSITIONS = Map.of(
            ACTIVE, Set.of(SUSPENDED),
            SUSPENDED, Set.of(ACTIVE)
    );

    private final String displayName;

    MemberStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitTo(MemberStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public MemberStatus transitTo(MemberStatus target) {
        if (!canTransitTo(target)) {
            throw new IllegalStateException(
                    displayName + " 상태에서 " + target.displayName + " 상태로 전이할 수 없습니다");
        }
        return target;
    }

    public String displayName() {
        return displayName;
    }
}

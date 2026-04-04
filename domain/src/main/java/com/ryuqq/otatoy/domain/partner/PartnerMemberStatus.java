package com.ryuqq.otatoy.domain.partner;

/**
 * 파트너 멤버 상태.
 * ACTIVE: 활성, SUSPENDED: 정지.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum PartnerMemberStatus {

    ACTIVE("활성"),
    SUSPENDED("정지");

    private final String displayName;

    PartnerMemberStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

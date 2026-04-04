package com.ryuqq.otatoy.domain.partner;

/**
 * 파트너 멤버 역할.
 * OWNER: 소유자, MANAGER: 관리자, STAFF: 직원.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum PartnerMemberRole {

    OWNER("소유자"),
    MANAGER("관리자"),
    STAFF("직원");

    private final String displayName;

    PartnerMemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

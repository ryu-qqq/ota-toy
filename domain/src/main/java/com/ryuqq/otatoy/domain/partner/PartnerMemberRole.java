package com.ryuqq.otatoy.domain.partner;

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

package com.ryuqq.otatoy.domain.partner;

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

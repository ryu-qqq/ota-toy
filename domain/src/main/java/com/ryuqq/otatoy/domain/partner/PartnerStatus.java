package com.ryuqq.otatoy.domain.partner;

public enum PartnerStatus {

    ACTIVE("활성"),
    SUSPENDED("정지");

    private final String displayName;

    PartnerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

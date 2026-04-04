package com.ryuqq.otatoy.domain.property;

public enum PropertyStatus {

    ACTIVE("운영중"),
    INACTIVE("비활성");

    private final String displayName;

    PropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

package com.ryuqq.otatoy.domain.supplier;

public enum SupplierStatus {

    ACTIVE("활성"),
    SUSPENDED("정지"),
    TERMINATED("해지");

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

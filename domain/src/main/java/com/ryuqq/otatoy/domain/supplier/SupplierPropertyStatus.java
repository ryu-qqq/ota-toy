package com.ryuqq.otatoy.domain.supplier;

public enum SupplierPropertyStatus {

    MAPPED("매핑됨"),
    UNMAPPED("매핑 해제");

    private final String displayName;

    SupplierPropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

package com.ryuqq.otatoy.domain.supplier;

public enum SupplierMappingStatus {

    MAPPED("매핑됨"),
    UNMAPPED("매핑해제");

    private final String displayName;

    SupplierMappingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

package com.ryuqq.otatoy.domain.supplier;

public enum SupplierSyncStatus {

    SUCCESS("성공"),
    FAILED("실패"),
    IN_PROGRESS("진행 중");

    private final String displayName;

    SupplierSyncStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

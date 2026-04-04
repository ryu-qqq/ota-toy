package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 매핑 상태.
 * MAPPED: 매핑됨, UNMAPPED: 매핑 해제.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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

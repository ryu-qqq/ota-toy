package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 상태.
 * ACTIVE: 운영 중, INACTIVE: 비활성.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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

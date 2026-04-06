package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 작업 유형.
 * PROPERTY_CONTENT: 숙소+객실 컨텐츠 수집 (Content API 대응).
 * RATE_AVAILABILITY: 요금+재고 수집 (Shopping API 대응).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum SupplierTaskType {

    PROPERTY_CONTENT("숙소 컨텐츠 수집"),
    RATE_AVAILABILITY("요금/재고 수집");

    private final String displayName;

    SupplierTaskType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

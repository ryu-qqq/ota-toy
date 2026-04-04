package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 공급 소스 유형.
 * DIRECT: 직접 입점, SUPPLIER: 외부 공급자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum SourceType {

    DIRECT("직접 입점"),
    SUPPLIER("외부 공급자");

    private final String displayName;

    SourceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

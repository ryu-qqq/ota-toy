package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 API 유형.
 * 각 유형에 따라 다른 데이터 수집/파싱 전략이 적용된다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum SupplierApiType {

    MOCK("Mock 테스트용");

    private final String displayName;

    SupplierApiType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

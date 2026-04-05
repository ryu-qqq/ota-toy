package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.common.sort.SortKey;

/**
 * 숙소 검색 결과 정렬 키.
 * 실제 OTA 플랫폼 분석 결과, 고객 검색에서 가격순 정렬이 핵심이다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum PropertySortKey implements SortKey {

    PRICE_LOW("priceLow", "가격 낮은순"),
    PRICE_HIGH("priceHigh", "가격 높은순");

    private final String fieldName;
    private final String displayName;

    PropertySortKey(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    public String displayName() {
        return displayName;
    }
}

package com.ryuqq.otatoy.api.customer.search;

/**
 * Customer 숙소 검색 API 엔드포인트 상수.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class CustomerSearchEndpoints {

    private CustomerSearchEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String BASE = "/api/v1/search";
    public static final String PROPERTIES = BASE + "/properties";
}

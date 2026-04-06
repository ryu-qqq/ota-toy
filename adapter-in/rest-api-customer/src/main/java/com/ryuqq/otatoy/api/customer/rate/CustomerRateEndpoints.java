package com.ryuqq.otatoy.api.customer.rate;

/**
 * Customer 요금 조회 API 엔드포인트 상수.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class CustomerRateEndpoints {

    private CustomerRateEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String BASE = "/api/v1/properties";
    public static final String PROPERTY_ID = "/{propertyId}";
    public static final String RATES = PROPERTY_ID + "/rates";

    public static final String PATH_PROPERTY_ID = "propertyId";
}

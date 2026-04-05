package com.ryuqq.otatoy.api.extranet.pricing;

import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;

/**
 * Extranet RatePlan API 엔드포인트 상수.
 * 객실 유형 하위 리소스로 요금 정책을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetRatePlanEndpoints {

    private ExtranetRatePlanEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans */
    public static final String BASE = ExtranetPropertyEndpoints.ROOMS + "/{roomTypeId}/rate-plans";

    /** /api/v1/extranet/rate-plans/{ratePlanId}/rates — 요금/재고 설정 */
    public static final String RATE_PLAN_RATES = "/api/v1/extranet/rate-plans/{ratePlanId}/rates";

    // Path Variable
    public static final String PATH_ROOM_TYPE_ID = "roomTypeId";
    public static final String PATH_RATE_PLAN_ID = "ratePlanId";
}

package com.ryuqq.otatoy.api.extranet.pricing;

import com.ryuqq.otatoy.api.extranet.common.ExtranetBaseEndpoints;

/**
 * Extranet RatePlan API 엔드포인트 상수.
 * Pricing BC 전용 — Property, RoomType은 각자의 Endpoints를 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetRatePlanEndpoints {

    private ExtranetRatePlanEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans */
    public static final String RATE_PLANS = ExtranetBaseEndpoints.BASE + "/properties/{propertyId}/rooms/{roomTypeId}/rate-plans";

    /** /api/v1/extranet/rate-plans/{ratePlanId}/rates — 요금/재고 설정 */
    public static final String RATE_PLAN_RATES = ExtranetBaseEndpoints.BASE + "/rate-plans/{ratePlanId}/rates";

    // Path Variable
    public static final String PATH_PROPERTY_ID = "propertyId";
    public static final String PATH_ROOM_TYPE_ID = "roomTypeId";
    public static final String PATH_RATE_PLAN_ID = "ratePlanId";
}

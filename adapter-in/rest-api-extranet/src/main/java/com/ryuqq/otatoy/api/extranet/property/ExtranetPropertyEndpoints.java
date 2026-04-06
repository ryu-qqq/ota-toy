package com.ryuqq.otatoy.api.extranet.property;

import com.ryuqq.otatoy.api.extranet.common.ExtranetBaseEndpoints;

/**
 * Extranet Property API 엔드포인트 상수.
 * Property BC 전용 — RoomType, RatePlan은 각자의 Endpoints를 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetPropertyEndpoints {

    private ExtranetPropertyEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 숙소
    public static final String PROPERTIES = ExtranetBaseEndpoints.BASE + "/properties";
    public static final String PROPERTY_ID = "/{propertyId}";
    public static final String PROPERTY_BY_ID = PROPERTIES + PROPERTY_ID;

    // 숙소 부속 (PROPERTIES 기준 상대 경로 — @RequestMapping(PROPERTIES) 하위에서 사용)
    public static final String REL_PHOTOS = PROPERTY_ID + "/photos";
    public static final String REL_AMENITIES = PROPERTY_ID + "/amenities";
    public static final String REL_ATTRIBUTES = PROPERTY_ID + "/attributes";

    // Path Variable
    public static final String PATH_PROPERTY_ID = "propertyId";
}

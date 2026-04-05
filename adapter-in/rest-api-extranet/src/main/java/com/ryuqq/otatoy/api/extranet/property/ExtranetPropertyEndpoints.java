package com.ryuqq.otatoy.api.extranet.property;

/**
 * Extranet Property API 엔드포인트 상수.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetPropertyEndpoints {

    private ExtranetPropertyEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String BASE = "/api/v1/extranet";

    // 숙소
    public static final String PROPERTIES = BASE + "/properties";
    public static final String PROPERTY_ID = "/{propertyId}";
    public static final String PROPERTY_BY_ID = PROPERTIES + PROPERTY_ID;

    // 숙소 부속
    public static final String PHOTOS = PROPERTY_BY_ID + "/photos";
    public static final String AMENITIES = PROPERTY_BY_ID + "/amenities";
    public static final String ATTRIBUTES = PROPERTY_BY_ID + "/attributes";
    public static final String ROOMS = PROPERTY_BY_ID + "/rooms";

    // Path Variable
    public static final String PATH_PROPERTY_ID = "propertyId";
}

package com.ryuqq.otatoy.api.extranet.roomtype;

import com.ryuqq.otatoy.api.extranet.common.ExtranetBaseEndpoints;

/**
 * Extranet RoomType API 엔드포인트 상수.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class ExtranetRoomTypeEndpoints {

    private ExtranetRoomTypeEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** /api/v1/extranet/properties/{propertyId}/rooms */
    public static final String ROOMS = ExtranetBaseEndpoints.BASE + "/properties/{propertyId}/rooms";

    // Path Variable
    public static final String PATH_PROPERTY_ID = "propertyId";
}

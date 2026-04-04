package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeAttribute(
        Long id,
        RoomTypeId roomTypeId,
        String attributeKey,
        String attributeValue
) {

    public static RoomTypeAttribute of(RoomTypeId roomTypeId, String attributeKey, String attributeValue) {
        return new RoomTypeAttribute(null, roomTypeId, attributeKey, attributeValue);
    }

    public static RoomTypeAttribute reconstitute(Long id, RoomTypeId roomTypeId,
                                                  String attributeKey, String attributeValue) {
        return new RoomTypeAttribute(id, roomTypeId, attributeKey, attributeValue);
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeAttribute(
        Long id,
        RoomTypeId roomTypeId,
        String attributeKey,
        String attributeValue
) {

    public RoomTypeAttribute {
        if (attributeKey == null || attributeKey.isBlank()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
    }

    public static RoomTypeAttribute of(RoomTypeId roomTypeId, String attributeKey, String attributeValue) {
        return new RoomTypeAttribute(null, roomTypeId, attributeKey, attributeValue);
    }

    public static RoomTypeAttribute reconstitute(Long id, RoomTypeId roomTypeId,
                                                  String attributeKey, String attributeValue) {
        return new RoomTypeAttribute(id, roomTypeId, attributeKey, attributeValue);
    }
}

package com.ryuqq.otatoy.domain.roomtype;

import java.util.Objects;

/**
 * 객실 유형의 추가 속성을 나타내는 엔티티.
 * 키-값 쌍으로 다양한 객실 부가 정보를 저장한다.
 */
public class RoomTypeAttribute {

    private final RoomTypeAttributeId id;
    private final RoomTypeId roomTypeId;
    private final String attributeKey;
    private final String attributeValue;

    private RoomTypeAttribute(RoomTypeAttributeId id, RoomTypeId roomTypeId, String attributeKey, String attributeValue) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.attributeKey = attributeKey;
        this.attributeValue = attributeValue;
    }

    public static RoomTypeAttribute forNew(RoomTypeId roomTypeId, String attributeKey, String attributeValue) {
        validate(attributeKey);
        return new RoomTypeAttribute(RoomTypeAttributeId.of(null), roomTypeId, attributeKey, attributeValue);
    }

    private static void validate(String attributeKey) {
        if (attributeKey == null || attributeKey.isBlank()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
    }

    public static RoomTypeAttribute reconstitute(RoomTypeAttributeId id, RoomTypeId roomTypeId,
                                                  String attributeKey, String attributeValue) {
        return new RoomTypeAttribute(id, roomTypeId, attributeKey, attributeValue);
    }

    public RoomTypeAttributeId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String attributeKey() { return attributeKey; }
    public String attributeValue() { return attributeValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomTypeAttribute r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package com.ryuqq.otatoy.domain.roomtype;

import java.time.Instant;
import java.util.Objects;

/**
 * 객실 유형의 추가 속성을 나타내는 엔티티.
 * 키-값 쌍으로 다양한 객실 부가 정보를 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class RoomTypeAttribute {

    private final RoomTypeAttributeId id;
    private final RoomTypeId roomTypeId;
    private final String attributeKey;
    private final String attributeValue;
    private final Instant createdAt;
    private Instant updatedAt;

    private RoomTypeAttribute(RoomTypeAttributeId id, RoomTypeId roomTypeId, String attributeKey, String attributeValue,
                               Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.attributeKey = attributeKey;
        this.attributeValue = attributeValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RoomTypeAttribute forNew(RoomTypeId roomTypeId, String attributeKey, String attributeValue, Instant now) {
        validate(attributeKey);
        return new RoomTypeAttribute(RoomTypeAttributeId.of(null), roomTypeId, attributeKey, attributeValue, now, now);
    }

    private static void validate(String attributeKey) {
        if (attributeKey == null || attributeKey.isBlank()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
    }

    public static RoomTypeAttribute reconstitute(RoomTypeAttributeId id, RoomTypeId roomTypeId,
                                                  String attributeKey, String attributeValue,
                                                  Instant createdAt, Instant updatedAt) {
        return new RoomTypeAttribute(id, roomTypeId, attributeKey, attributeValue, createdAt, updatedAt);
    }

    public RoomTypeAttributeId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String attributeKey() { return attributeKey; }
    public String attributeValue() { return attributeValue; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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

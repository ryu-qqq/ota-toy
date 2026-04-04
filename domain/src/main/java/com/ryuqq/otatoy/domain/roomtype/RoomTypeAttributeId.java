package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 유형 속성 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RoomTypeAttributeId(Long value) {

    public static RoomTypeAttributeId of(Long value) {
        return new RoomTypeAttributeId(value);
    }

    public static RoomTypeAttributeId forNew() { return new RoomTypeAttributeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

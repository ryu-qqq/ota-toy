package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 유형 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RoomTypeId(Long value) {

    public static RoomTypeId of(Long value) {
        return new RoomTypeId(value);
    }

    public static RoomTypeId forNew() { return new RoomTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

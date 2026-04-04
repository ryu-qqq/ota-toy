package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 침대 구성 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RoomTypeBedId(Long value) {

    public static RoomTypeBedId of(Long value) {
        return new RoomTypeBedId(value);
    }

    public static RoomTypeBedId forNew() { return new RoomTypeBedId(null); }

    public boolean isNew() {
        return value == null;
    }
}

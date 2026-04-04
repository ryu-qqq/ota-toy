package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 전망 매핑 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RoomTypeViewId(Long value) {

    public static RoomTypeViewId of(Long value) {
        return new RoomTypeViewId(value);
    }

    public static RoomTypeViewId forNew() { return new RoomTypeViewId(null); }

    public boolean isNew() {
        return value == null;
    }
}

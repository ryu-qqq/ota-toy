package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 편의시설 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record RoomAmenityId(Long value) {

    public static RoomAmenityId of(Long value) {
        return new RoomAmenityId(value);
    }

    public static RoomAmenityId forNew() { return new RoomAmenityId(null); }

    public boolean isNew() {
        return value == null;
    }
}

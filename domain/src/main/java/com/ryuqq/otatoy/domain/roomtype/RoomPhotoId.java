package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 사진 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record RoomPhotoId(Long value) {

    public static RoomPhotoId of(Long value) {
        return new RoomPhotoId(value);
    }

    public static RoomPhotoId forNew() { return new RoomPhotoId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.roomtype;

public record RoomPhotoId(Long value) {

    public static RoomPhotoId of(Long value) {
        return new RoomPhotoId(value);
    }

    public static RoomPhotoId forNew() { return new RoomPhotoId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record RoomPhotoId(Long value) {

    public static RoomPhotoId of(Long value) {
        return new RoomPhotoId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record RoomAmenityId(Long value) {

    public static RoomAmenityId of(Long value) {
        return new RoomAmenityId(value);
    }

    public static RoomAmenityId forNew() { return new RoomAmenityId(null); }

    public boolean isNew() {
        return value == null;
    }
}

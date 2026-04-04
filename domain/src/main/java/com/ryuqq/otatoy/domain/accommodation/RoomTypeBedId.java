package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeBedId(Long value) {

    public static RoomTypeBedId of(Long value) {
        return new RoomTypeBedId(value);
    }

    public static RoomTypeBedId forNew() { return new RoomTypeBedId(null); }

    public boolean isNew() {
        return value == null;
    }
}

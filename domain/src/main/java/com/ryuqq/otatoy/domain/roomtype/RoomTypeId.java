package com.ryuqq.otatoy.domain.roomtype;

public record RoomTypeId(Long value) {

    public static RoomTypeId of(Long value) {
        return new RoomTypeId(value);
    }

    public static RoomTypeId forNew() { return new RoomTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

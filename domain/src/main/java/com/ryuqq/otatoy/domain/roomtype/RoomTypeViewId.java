package com.ryuqq.otatoy.domain.roomtype;

public record RoomTypeViewId(Long value) {

    public static RoomTypeViewId of(Long value) {
        return new RoomTypeViewId(value);
    }

    public static RoomTypeViewId forNew() { return new RoomTypeViewId(null); }

    public boolean isNew() {
        return value == null;
    }
}

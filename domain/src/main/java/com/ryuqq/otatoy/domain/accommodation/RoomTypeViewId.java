package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeViewId(Long value) {

    public static RoomTypeViewId of(Long value) {
        return new RoomTypeViewId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeId(Long value) {

    public static RoomTypeId of(Long value) {
        return new RoomTypeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

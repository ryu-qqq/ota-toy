package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeAttributeId(Long value) {

    public static RoomTypeAttributeId of(Long value) {
        return new RoomTypeAttributeId(value);
    }

    public static RoomTypeAttributeId forNew() { return new RoomTypeAttributeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

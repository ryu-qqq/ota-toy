package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeDescription(String value) {

    public static RoomTypeDescription of(String value) {
        return new RoomTypeDescription(value);
    }
}

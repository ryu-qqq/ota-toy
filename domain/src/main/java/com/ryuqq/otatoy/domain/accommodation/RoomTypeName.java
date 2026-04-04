package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeName(String value) {

    public RoomTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("객실 유형명은 필수입니다");
        }
    }

    public static RoomTypeName of(String value) {
        return new RoomTypeName(value);
    }
}

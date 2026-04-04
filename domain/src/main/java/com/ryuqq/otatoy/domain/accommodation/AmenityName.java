package com.ryuqq.otatoy.domain.accommodation;

public record AmenityName(String value) {

    public AmenityName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("편의시설명은 필수입니다");
        }
    }

    public static AmenityName of(String value) {
        return new AmenityName(value);
    }
}

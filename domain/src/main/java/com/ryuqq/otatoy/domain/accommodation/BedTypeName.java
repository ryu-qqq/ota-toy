package com.ryuqq.otatoy.domain.accommodation;

public record BedTypeName(String value) {

    public BedTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("침대 유형명은 필수입니다");
        }
    }

    public static BedTypeName of(String value) {
        return new BedTypeName(value);
    }
}

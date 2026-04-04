package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeDescription(String value) {

    public static PropertyTypeDescription of(String value) {
        return new PropertyTypeDescription(value);
    }
}

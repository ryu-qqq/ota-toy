package com.ryuqq.otatoy.domain.accommodation;

public record ViewTypeName(String value) {

    public ViewTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전망 유형명은 필수입니다");
        }
    }

    public static ViewTypeName of(String value) {
        return new ViewTypeName(value);
    }
}

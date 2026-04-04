package com.ryuqq.otatoy.domain.pricing;

public record AddOnType(String value) {

    public AddOnType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Add-on 유형은 필수입니다");
        }
    }

    public static AddOnType of(String value) {
        return new AddOnType(value);
    }
}

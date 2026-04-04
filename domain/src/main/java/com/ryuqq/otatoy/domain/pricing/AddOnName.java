package com.ryuqq.otatoy.domain.pricing;

public record AddOnName(String value) {

    public AddOnName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Add-on 이름은 필수입니다");
        }
    }

    public static AddOnName of(String value) {
        return new AddOnName(value);
    }
}

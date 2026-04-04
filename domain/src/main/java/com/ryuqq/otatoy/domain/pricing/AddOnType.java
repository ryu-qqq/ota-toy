package com.ryuqq.otatoy.domain.pricing;

public record AddOnType(String value) {

    private static final int MAX_LENGTH = 50;

    public AddOnType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Add-on 유형은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Add-on 유형은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static AddOnType of(String value) {
        return new AddOnType(value);
    }
}

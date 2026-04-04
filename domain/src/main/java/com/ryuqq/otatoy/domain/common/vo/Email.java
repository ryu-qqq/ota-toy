package com.ryuqq.otatoy.domain.common.vo;

public record Email(String value) {

    private static final int MAX_LENGTH = 200;

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("이메일은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
        if (!value.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}

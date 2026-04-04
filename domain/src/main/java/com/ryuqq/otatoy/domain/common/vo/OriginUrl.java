package com.ryuqq.otatoy.domain.common.vo;

public record OriginUrl(String value) {

    private static final int MAX_LENGTH = 500;

    public OriginUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("원본 URL은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("원본 URL은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static OriginUrl of(String value) {
        return new OriginUrl(value);
    }
}

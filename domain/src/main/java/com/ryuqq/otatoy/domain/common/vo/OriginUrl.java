package com.ryuqq.otatoy.domain.common.vo;

public record OriginUrl(String value) {

    public OriginUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("원본 URL은 필수입니다");
        }
    }

    public static OriginUrl of(String value) {
        return new OriginUrl(value);
    }
}

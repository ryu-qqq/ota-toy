package com.ryuqq.otatoy.domain.brand;

/**
 * 브랜드 영문명. null/blank 불가, 최대 100자.
 */
public record BrandName(String value) {

    private static final int MAX_LENGTH = 100;

    public BrandName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("브랜드명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static BrandName of(String value) {
        return new BrandName(value);
    }
}

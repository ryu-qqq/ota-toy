package com.ryuqq.otatoy.domain.accommodation;

public record BrandId(Long value) {

    public BrandId {
        if (value == null) {
            throw new IllegalArgumentException("브랜드 ID는 필수입니다");
        }
    }

    public static BrandId of(Long value) {
        return new BrandId(value);
    }
}

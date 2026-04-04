package com.ryuqq.otatoy.domain.location;

/**
 * 랜드마크명. null/blank 불가, 최대 200자.
 */
public record LandmarkName(String value) {

    private static final int MAX_LENGTH = 200;

    public LandmarkName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("랜드마크명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("랜드마크명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static LandmarkName of(String value) {
        return new LandmarkName(value);
    }
}

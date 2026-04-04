package com.ryuqq.otatoy.domain.supplier;

/**
 * 대표자명 VO. NOT NULL이며, 100자 이하를 보장한다.
 */
public record OwnerName(String value) {

    private static final int MAX_LENGTH = 100;

    public OwnerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("대표자명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("대표자명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static OwnerName of(String value) {
        return new OwnerName(value);
    }
}

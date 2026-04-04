package com.ryuqq.otatoy.domain.supplier;

/**
 * 사업자번호 VO. NOT NULL이며, 50자 이하를 보장한다.
 */
public record BusinessNo(String value) {

    private static final int MAX_LENGTH = 50;

    public BusinessNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("사업자번호는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("사업자번호는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static BusinessNo of(String value) {
        return new BusinessNo(value);
    }
}

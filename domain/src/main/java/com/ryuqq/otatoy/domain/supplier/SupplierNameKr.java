package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 한글명 VO. nullable이며, 값이 있을 경우 200자 이하를 보장한다.
 */
public record SupplierNameKr(String value) {

    private static final int MAX_LENGTH = 200;

    public SupplierNameKr {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("공급자 한글명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static SupplierNameKr of(String value) {
        return new SupplierNameKr(value);
    }
}

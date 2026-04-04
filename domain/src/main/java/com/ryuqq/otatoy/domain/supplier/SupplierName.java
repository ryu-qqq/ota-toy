package com.ryuqq.otatoy.domain.supplier;

public record SupplierName(String value) {

    private static final int MAX_LENGTH = 200;

    public SupplierName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("공급자명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("공급자명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static SupplierName of(String value) {
        return new SupplierName(value);
    }
}

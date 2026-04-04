package com.ryuqq.otatoy.domain.supplier;

public record SupplierName(String value) {

    public SupplierName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("공급자명은 필수입니다");
        }
    }

    public static SupplierName of(String value) {
        return new SupplierName(value);
    }
}

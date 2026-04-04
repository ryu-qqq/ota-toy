package com.ryuqq.otatoy.domain.supplier;

public record SupplierPropertyId(Long value) {

    public static SupplierPropertyId of(Long value) {
        return new SupplierPropertyId(value);
    }

    public static SupplierPropertyId forNew() { return new SupplierPropertyId(null); }

    public boolean isNew() {
        return value == null;
    }
}

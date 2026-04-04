package com.ryuqq.otatoy.domain.supplier;

public record SupplierId(Long value) {

    public static SupplierId of(Long value) {
        return new SupplierId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

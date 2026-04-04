package com.ryuqq.otatoy.domain.supplier;

public record SupplierId(Long value) {

    public static SupplierId of(Long value) {
        return new SupplierId(value);
    }

    public static SupplierId forNew() { return new SupplierId(null); }

    public boolean isNew() {
        return value == null;
    }
}

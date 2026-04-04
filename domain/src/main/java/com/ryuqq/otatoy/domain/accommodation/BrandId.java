package com.ryuqq.otatoy.domain.accommodation;

public record BrandId(Long value) {

    public static BrandId of(Long value) {
        return new BrandId(value);
    }

    public static BrandId forNew() { return new BrandId(null); }

    public boolean isNew() {
        return value == null;
    }
}

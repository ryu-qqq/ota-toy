package com.ryuqq.otatoy.domain.accommodation;

public record PropertyAmenityId(Long value) {

    public static PropertyAmenityId of(Long value) {
        return new PropertyAmenityId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

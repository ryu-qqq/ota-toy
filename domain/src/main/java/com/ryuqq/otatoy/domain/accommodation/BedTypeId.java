package com.ryuqq.otatoy.domain.accommodation;

public record BedTypeId(Long value) {

    public static BedTypeId of(Long value) {
        return new BedTypeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

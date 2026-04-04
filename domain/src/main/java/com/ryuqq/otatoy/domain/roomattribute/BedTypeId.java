package com.ryuqq.otatoy.domain.roomattribute;

public record BedTypeId(Long value) {

    public static BedTypeId of(Long value) {
        return new BedTypeId(value);
    }

    public static BedTypeId forNew() { return new BedTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

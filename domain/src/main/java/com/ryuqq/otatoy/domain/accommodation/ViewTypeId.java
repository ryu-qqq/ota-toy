package com.ryuqq.otatoy.domain.accommodation;

public record ViewTypeId(Long value) {

    public static ViewTypeId of(Long value) {
        return new ViewTypeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.location;

public record LandmarkId(Long value) {

    public static LandmarkId of(Long value) {
        return new LandmarkId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

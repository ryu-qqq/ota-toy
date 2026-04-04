package com.ryuqq.otatoy.domain.location;

public record PropertyLandmarkId(Long value) {

    public static PropertyLandmarkId of(Long value) {
        return new PropertyLandmarkId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

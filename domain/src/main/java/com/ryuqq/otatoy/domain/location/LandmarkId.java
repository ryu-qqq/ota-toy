package com.ryuqq.otatoy.domain.location;

public record LandmarkId(Long value) {

    public static LandmarkId of(Long value) {
        return new LandmarkId(value);
    }

    public static LandmarkId forNew() { return new LandmarkId(null); }

    public boolean isNew() {
        return value == null;
    }
}

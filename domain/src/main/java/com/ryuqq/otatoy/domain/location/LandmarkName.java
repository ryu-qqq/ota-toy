package com.ryuqq.otatoy.domain.location;

public record LandmarkName(String value) {

    public LandmarkName {
        if (value == null || value.isBlank()) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_NAME);
        }
    }

    public static LandmarkName of(String value) {
        return new LandmarkName(value);
    }
}

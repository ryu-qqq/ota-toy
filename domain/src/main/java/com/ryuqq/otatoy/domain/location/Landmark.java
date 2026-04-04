package com.ryuqq.otatoy.domain.location;

import java.util.Objects;

public class Landmark {

    private final LandmarkId id;
    private final LandmarkName name;
    private final LandmarkType landmarkType;
    private final double latitude;
    private final double longitude;

    private Landmark(LandmarkId id, LandmarkName name, LandmarkType landmarkType, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.landmarkType = landmarkType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Landmark forNew(LandmarkName name, LandmarkType landmarkType, double latitude, double longitude) {
        if (name == null) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_NAME);
        }
        if (landmarkType == null) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_TYPE);
        }
        if (latitude < -90 || latitude > 90) {
            throw new LocationException(LocationErrorCode.INVALID_LATITUDE);
        }
        if (longitude < -180 || longitude > 180) {
            throw new LocationException(LocationErrorCode.INVALID_LONGITUDE);
        }
        return new Landmark(LandmarkId.of(null), name, landmarkType, latitude, longitude);
    }

    public static Landmark reconstitute(LandmarkId id, LandmarkName name, LandmarkType landmarkType,
                                         double latitude, double longitude) {
        return new Landmark(id, name, landmarkType, latitude, longitude);
    }

    public LandmarkId id() { return id; }
    public LandmarkName name() { return name; }
    public LandmarkType landmarkType() { return landmarkType; }
    public double latitude() { return latitude; }
    public double longitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Landmark l)) return false;
        return id != null && id.equals(l.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

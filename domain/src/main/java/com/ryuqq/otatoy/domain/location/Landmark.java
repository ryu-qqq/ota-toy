package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.common.vo.Coordinate;

import java.util.Objects;

public class Landmark {

    private final LandmarkId id;
    private final LandmarkName name;
    private final LandmarkType landmarkType;
    private final Coordinate coordinate;

    private Landmark(LandmarkId id, LandmarkName name, LandmarkType landmarkType, Coordinate coordinate) {
        this.id = id;
        this.name = name;
        this.landmarkType = landmarkType;
        this.coordinate = coordinate;
    }

    public static Landmark forNew(LandmarkName name, LandmarkType landmarkType, double latitude, double longitude) {
        validateRequired(name, landmarkType);
        return new Landmark(LandmarkId.of(null), name, landmarkType, Coordinate.of(latitude, longitude));
    }

    private static void validateRequired(LandmarkName name, LandmarkType landmarkType) {
        if (name == null) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_NAME);
        }
        if (landmarkType == null) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_TYPE);
        }
    }

    public static Landmark reconstitute(LandmarkId id, LandmarkName name, LandmarkType landmarkType,
                                         double latitude, double longitude) {
        return new Landmark(id, name, landmarkType, Coordinate.of(latitude, longitude));
    }

    public LandmarkId id() { return id; }
    public LandmarkName name() { return name; }
    public LandmarkType landmarkType() { return landmarkType; }
    public Coordinate coordinate() { return coordinate; }
    public double latitude() { return coordinate.latitude(); }
    public double longitude() { return coordinate.longitude(); }

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

package com.ryuqq.otatoy.domain.location;

import java.util.Objects;

/**
 * 숙소와 랜드마크 간의 매핑 관계를 나타내는 엔티티.
 * 숙소로부터의 거리(km)와 도보 시간(분)을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PropertyLandmark {

    private final PropertyLandmarkId id;
    private final long propertyId;
    private final LandmarkId landmarkId;
    private final double distanceKm;
    private final int walkingMinutes;

    private PropertyLandmark(PropertyLandmarkId id, long propertyId, LandmarkId landmarkId,
                              double distanceKm, int walkingMinutes) {
        this.id = id;
        this.propertyId = propertyId;
        this.landmarkId = landmarkId;
        this.distanceKm = distanceKm;
        this.walkingMinutes = walkingMinutes;
    }

    public static PropertyLandmark forNew(long propertyId, LandmarkId landmarkId,
                                           double distanceKm, int walkingMinutes) {
        validateRequired(propertyId, landmarkId);
        validateMeasurements(distanceKm, walkingMinutes);
        return new PropertyLandmark(PropertyLandmarkId.of(null), propertyId, landmarkId, distanceKm, walkingMinutes);
    }

    private static void validateRequired(long propertyId, LandmarkId landmarkId) {
        if (propertyId <= 0) {
            throw new LocationException(LocationErrorCode.INVALID_PROPERTY_ID);
        }
        if (landmarkId == null || landmarkId.value() == null) {
            throw new LocationException(LocationErrorCode.INVALID_LANDMARK_ID);
        }
    }

    private static void validateMeasurements(double distanceKm, int walkingMinutes) {
        if (distanceKm < 0) {
            throw new LocationException(LocationErrorCode.INVALID_DISTANCE);
        }
        if (walkingMinutes < 0) {
            throw new LocationException(LocationErrorCode.INVALID_WALKING_MINUTES);
        }
    }

    public static PropertyLandmark reconstitute(PropertyLandmarkId id, long propertyId, LandmarkId landmarkId,
                                                 double distanceKm, int walkingMinutes) {
        return new PropertyLandmark(id, propertyId, landmarkId, distanceKm, walkingMinutes);
    }

    public PropertyLandmarkId id() { return id; }
    public long propertyId() { return propertyId; }
    public LandmarkId landmarkId() { return landmarkId; }
    public double distanceKm() { return distanceKm; }
    public int walkingMinutes() { return walkingMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyLandmark p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

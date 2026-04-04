package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.accommodation.PropertyId;

import java.util.Objects;

public class PropertyLandmark {

    private final PropertyLandmarkId id;
    private final PropertyId propertyId;
    private final LandmarkId landmarkId;
    private final double distanceKm;
    private final int walkingMinutes;

    private PropertyLandmark(PropertyLandmarkId id, PropertyId propertyId, LandmarkId landmarkId,
                              double distanceKm, int walkingMinutes) {
        this.id = id;
        this.propertyId = propertyId;
        this.landmarkId = landmarkId;
        this.distanceKm = distanceKm;
        this.walkingMinutes = walkingMinutes;
    }

    public static PropertyLandmark forNew(PropertyId propertyId, LandmarkId landmarkId,
                                           double distanceKm, int walkingMinutes) {
        if (landmarkId == null || landmarkId.value() == null) {
            throw new IllegalArgumentException("랜드마크 ID는 필수입니다");
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("거리는 0 이상이어야 합니다");
        }
        if (walkingMinutes < 0) {
            throw new IllegalArgumentException("도보 시간은 0 이상이어야 합니다");
        }
        return new PropertyLandmark(PropertyLandmarkId.of(null), propertyId, landmarkId, distanceKm, walkingMinutes);
    }

    public static PropertyLandmark reconstitute(PropertyLandmarkId id, PropertyId propertyId, LandmarkId landmarkId,
                                                 double distanceKm, int walkingMinutes) {
        return new PropertyLandmark(id, propertyId, landmarkId, distanceKm, walkingMinutes);
    }

    public PropertyLandmarkId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
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

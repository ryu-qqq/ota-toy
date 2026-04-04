package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.accommodation.PropertyId;

public record PropertyLandmark(
        Long id,
        PropertyId propertyId,
        Long landmarkId,
        double distanceKm,
        int walkingMinutes
) {

    public PropertyLandmark {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("거리는 0 이상이어야 합니다");
        }
        if (walkingMinutes < 0) {
            throw new IllegalArgumentException("도보 시간은 0 이상이어야 합니다");
        }
    }

    public static PropertyLandmark of(PropertyId propertyId, Long landmarkId,
                                       double distanceKm, int walkingMinutes) {
        return new PropertyLandmark(null, propertyId, landmarkId, distanceKm, walkingMinutes);
    }

    public static PropertyLandmark reconstitute(Long id, PropertyId propertyId, Long landmarkId,
                                                 double distanceKm, int walkingMinutes) {
        return new PropertyLandmark(id, propertyId, landmarkId, distanceKm, walkingMinutes);
    }
}

package com.ryuqq.otatoy.domain.location;

/**
 * Location BC 테스트용 Fixture.
 * 다양한 상태의 Location, Landmark, PropertyLandmark 객체를 생성한다.
 */
public final class LocationFixture {

    private LocationFixture() {}

    // === 기본 상수 ===
    public static final long DEFAULT_PROPERTY_ID = 1L;
    public static final double DEFAULT_LATITUDE = 37.5665;
    public static final double DEFAULT_LONGITUDE = 126.9780;
    public static final double DEFAULT_DISTANCE_KM = 1.5;
    public static final int DEFAULT_WALKING_MINUTES = 18;

    // === Location VO ===

    /**
     * 기본 Location VO (서울시 중구)
     */
    public static Location defaultLocation() {
        return Location.of("서울시 중구 명동길 14", DEFAULT_LATITUDE, DEFAULT_LONGITUDE, "명동", "서울");
    }

    /**
     * 지정 주소의 Location VO
     */
    public static Location locationWithAddress(String address) {
        return Location.of(address, DEFAULT_LATITUDE, DEFAULT_LONGITUDE, "명동", "서울");
    }

    /**
     * 지정 좌표의 Location VO
     */
    public static Location locationWithCoordinates(double latitude, double longitude) {
        return Location.of("서울시 중구", latitude, longitude, "명동", "서울");
    }

    // === Landmark ===

    /**
     * 기본 랜드마크 (서울역, STATION)
     */
    public static Landmark defaultLandmark() {
        return Landmark.forNew(
                LandmarkName.of("서울역"),
                LandmarkType.STATION,
                37.5547, 126.9707
        );
    }

    /**
     * 지정 타입의 랜드마크
     */
    public static Landmark landmarkOfType(LandmarkType type) {
        return Landmark.forNew(
                LandmarkName.of("테스트 랜드마크"),
                type,
                DEFAULT_LATITUDE, DEFAULT_LONGITUDE
        );
    }

    /**
     * DB 복원된 랜드마크 (id=1)
     */
    public static Landmark reconstitutedLandmark() {
        return Landmark.reconstitute(
                LandmarkId.of(1L),
                LandmarkName.of("서울역"),
                LandmarkType.STATION,
                37.5547, 126.9707
        );
    }

    /**
     * 지정 파라미터로 DB 복원 랜드마크
     */
    public static Landmark reconstituted(long id, String name, LandmarkType type,
                                          double latitude, double longitude) {
        return Landmark.reconstitute(
                LandmarkId.of(id), LandmarkName.of(name), type, latitude, longitude
        );
    }

    // === PropertyLandmark ===

    /**
     * 기본 PropertyLandmark (거리 1.5km, 도보 18분)
     */
    public static PropertyLandmark defaultPropertyLandmark() {
        return PropertyLandmark.forNew(
                DEFAULT_PROPERTY_ID,
                LandmarkId.of(1L),
                DEFAULT_DISTANCE_KM,
                DEFAULT_WALKING_MINUTES
        );
    }

    /**
     * 지정 거리/도보 시간의 PropertyLandmark
     */
    public static PropertyLandmark propertyLandmarkWithDistance(double distanceKm, int walkingMinutes) {
        return PropertyLandmark.forNew(
                DEFAULT_PROPERTY_ID,
                LandmarkId.of(1L),
                distanceKm,
                walkingMinutes
        );
    }

    /**
     * DB 복원된 PropertyLandmark (id=1)
     */
    public static PropertyLandmark reconstitutedPropertyLandmark() {
        return PropertyLandmark.reconstitute(
                PropertyLandmarkId.of(1L),
                DEFAULT_PROPERTY_ID,
                LandmarkId.of(1L),
                DEFAULT_DISTANCE_KM,
                DEFAULT_WALKING_MINUTES
        );
    }

    /**
     * 지정 파라미터로 DB 복원 PropertyLandmark
     */
    public static PropertyLandmark reconstitutedPropertyLandmark(long id, long propertyId, long landmarkId,
                                                                  double distanceKm, int walkingMinutes) {
        return PropertyLandmark.reconstitute(
                PropertyLandmarkId.of(id), propertyId, LandmarkId.of(landmarkId),
                distanceKm, walkingMinutes
        );
    }
}

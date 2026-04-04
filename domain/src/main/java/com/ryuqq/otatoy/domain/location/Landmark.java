package com.ryuqq.otatoy.domain.location;

import java.util.Objects;

public class Landmark {

    private final Long id;
    private final String name;
    private final LandmarkType landmarkType;
    private final double latitude;
    private final double longitude;

    private Landmark(Long id, String name, LandmarkType landmarkType, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.landmarkType = landmarkType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Landmark forNew(String name, LandmarkType landmarkType, double latitude, double longitude) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("랜드마크명은 필수입니다");
        }
        return new Landmark(null, name, landmarkType, latitude, longitude);
    }

    public static Landmark reconstitute(Long id, String name, LandmarkType landmarkType,
                                         double latitude, double longitude) {
        return new Landmark(id, name, landmarkType, latitude, longitude);
    }

    public Long id() { return id; }
    public String name() { return name; }
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

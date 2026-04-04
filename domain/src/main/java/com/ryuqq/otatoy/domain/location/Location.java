package com.ryuqq.otatoy.domain.location;

public record Location(
        String address,
        double latitude,
        double longitude,
        String neighborhood,
        String region
) {

    public Location {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도 범위 초과: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도 범위 초과: " + longitude);
        }
    }

    public static Location of(String address, double latitude, double longitude,
                               String neighborhood, String region) {
        return new Location(address, latitude, longitude, neighborhood, region);
    }
}

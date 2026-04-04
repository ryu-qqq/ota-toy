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
            throw new LocationException(LocationErrorCode.INVALID_ADDRESS);
        }
        if (latitude < -90 || latitude > 90) {
            throw new LocationException(LocationErrorCode.INVALID_LATITUDE);
        }
        if (longitude < -180 || longitude > 180) {
            throw new LocationException(LocationErrorCode.INVALID_LONGITUDE);
        }
    }

    public static Location of(String address, double latitude, double longitude,
                               String neighborhood, String region) {
        return new Location(address, latitude, longitude, neighborhood, region);
    }
}

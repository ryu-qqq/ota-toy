package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.common.vo.Coordinate;

/**
 * 숙소의 위치 정보를 나타내는 VO.
 * 주소(필수), 좌표(필수), 동네명, 지역명을 포함한다.
 */
public record Location(
        String address,
        Coordinate coordinate,
        String neighborhood,
        String region
) {

    public Location {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
        if (coordinate == null) {
            throw new IllegalArgumentException("좌표는 필수입니다");
        }
    }

    public static Location of(String address, double latitude, double longitude,
                               String neighborhood, String region) {
        return new Location(address, Coordinate.of(latitude, longitude), neighborhood, region);
    }

    /**
     * 위도 접근 편의 메서드
     */
    public double latitude() {
        return coordinate.latitude();
    }

    /**
     * 경도 접근 편의 메서드
     */
    public double longitude() {
        return coordinate.longitude();
    }
}

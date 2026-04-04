package com.ryuqq.otatoy.domain.common.vo;

/**
 * 위도/경도 좌표를 나타내는 VO.
 * 위도: -90 ~ 90, 경도: -180 ~ 180 범위를 보장한다.
 */
public record Coordinate(double latitude, double longitude) {

    public Coordinate {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도 범위가 올바르지 않습니다: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도 범위가 올바르지 않습니다: " + longitude);
        }
    }

    public static Coordinate of(double latitude, double longitude) {
        return new Coordinate(latitude, longitude);
    }
}

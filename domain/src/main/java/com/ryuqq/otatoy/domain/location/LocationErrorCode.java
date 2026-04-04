package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum LocationErrorCode implements ErrorCode {

    LANDMARK_NOT_FOUND("LOC-001", 404, "랜드마크를 찾을 수 없습니다"),
    INVALID_LANDMARK_NAME("LOC-002", 400, "랜드마크명은 필수입니다"),
    INVALID_LANDMARK_TYPE("LOC-003", 400, "랜드마크 유형은 필수입니다"),
    INVALID_LATITUDE("LOC-004", 400, "위도 범위가 올바르지 않습니다"),
    INVALID_LONGITUDE("LOC-005", 400, "경도 범위가 올바르지 않습니다"),
    INVALID_ADDRESS("LOC-006", 400, "주소는 필수입니다"),
    INVALID_PROPERTY_ID("LOC-007", 400, "숙소 ID는 필수입니다"),
    INVALID_LANDMARK_ID("LOC-008", 400, "랜드마크 ID는 필수입니다"),
    INVALID_DISTANCE("LOC-009", 400, "거리는 0 이상이어야 합니다"),
    INVALID_WALKING_MINUTES("LOC-010", 400, "도보 시간은 0 이상이어야 합니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    LocationErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

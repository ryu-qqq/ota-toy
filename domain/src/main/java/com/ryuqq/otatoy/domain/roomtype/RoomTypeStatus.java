package com.ryuqq.otatoy.domain.roomtype;

/**
 * 객실 유형 상태.
 * ACTIVE: 운영 중, INACTIVE: 비활성.
 */
public enum RoomTypeStatus {

    ACTIVE("운영중"),
    INACTIVE("비활성");

    private final String displayName;

    RoomTypeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

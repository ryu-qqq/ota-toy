package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 동기화 유형.
 * PROPERTY: 숙소, ROOM_TYPE: 객실, RATE: 요금, INVENTORY: 재고, FULL: 전체.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum SupplierSyncType {

    FETCH("데이터 수집"),
    PROPERTY("숙소 동기화"),
    ROOM_TYPE("객실 동기화"),
    RATE("요금 동기화"),
    INVENTORY("재고 동기화"),
    FULL("전체 동기화");

    private final String displayName;

    SupplierSyncType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

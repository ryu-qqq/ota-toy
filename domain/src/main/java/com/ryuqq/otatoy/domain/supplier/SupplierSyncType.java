package com.ryuqq.otatoy.domain.supplier;

public enum SupplierSyncType {

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

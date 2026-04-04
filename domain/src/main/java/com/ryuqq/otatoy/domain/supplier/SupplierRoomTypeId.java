package com.ryuqq.otatoy.domain.supplier;

public record SupplierRoomTypeId(Long value) {

    public static SupplierRoomTypeId of(Long value) {
        return new SupplierRoomTypeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

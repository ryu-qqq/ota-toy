package com.ryuqq.otatoy.domain.inventory;

public record InventoryId(Long value) {

    public static InventoryId of(Long value) {
        return new InventoryId(value);
    }

    public static InventoryId forNew() { return new InventoryId(null); }

    public boolean isNew() {
        return value == null;
    }
}

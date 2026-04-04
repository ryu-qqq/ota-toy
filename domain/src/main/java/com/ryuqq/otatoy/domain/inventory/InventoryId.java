package com.ryuqq.otatoy.domain.inventory;

/**
 * 재고 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record InventoryId(Long value) {

    public static InventoryId of(Long value) {
        return new InventoryId(value);
    }

    public static InventoryId forNew() { return new InventoryId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자-객실 매핑 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record SupplierRoomTypeId(Long value) {

    public static SupplierRoomTypeId of(Long value) {
        return new SupplierRoomTypeId(value);
    }

    public static SupplierRoomTypeId forNew() { return new SupplierRoomTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

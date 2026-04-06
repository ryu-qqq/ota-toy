package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 원시 데이터 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierRawDataId(Long value) {

    public static SupplierRawDataId of(Long value) {
        return new SupplierRawDataId(value);
    }

    public static SupplierRawDataId forNew() { return new SupplierRawDataId(null); }

    public boolean isNew() {
        return value == null;
    }
}

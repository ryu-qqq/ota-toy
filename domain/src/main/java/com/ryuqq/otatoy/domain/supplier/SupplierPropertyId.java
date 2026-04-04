package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자-숙소 매핑 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record SupplierPropertyId(Long value) {

    public static SupplierPropertyId of(Long value) {
        return new SupplierPropertyId(value);
    }

    public static SupplierPropertyId forNew() { return new SupplierPropertyId(null); }

    public boolean isNew() {
        return value == null;
    }
}

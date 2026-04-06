package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 작업 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierTaskId(Long value) {

    public static SupplierTaskId of(Long value) {
        return new SupplierTaskId(value);
    }

    public static SupplierTaskId forNew() {
        return new SupplierTaskId(null);
    }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record SupplierId(Long value) {

    public static SupplierId of(Long value) {
        return new SupplierId(value);
    }

    public static SupplierId forNew() { return new SupplierId(null); }

    public boolean isNew() {
        return value == null;
    }
}

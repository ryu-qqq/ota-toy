package com.ryuqq.otatoy.domain.brand;

/**
 * 브랜드 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record BrandId(Long value) {

    public static BrandId of(Long value) {
        return new BrandId(value);
    }

    public static BrandId forNew() { return new BrandId(null); }

    public boolean isNew() {
        return value == null;
    }
}

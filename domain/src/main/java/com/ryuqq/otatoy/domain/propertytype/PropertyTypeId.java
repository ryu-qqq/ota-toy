package com.ryuqq.otatoy.domain.propertytype;

/**
 * 숙소 유형 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PropertyTypeId(Long value) {

    public static PropertyTypeId of(Long value) {
        return new PropertyTypeId(value);
    }

    public static PropertyTypeId forNew() { return new PropertyTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

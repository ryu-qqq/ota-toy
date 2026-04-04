package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PropertyId(Long value) {

    public static PropertyId of(Long value) {
        return new PropertyId(value);
    }

    public static PropertyId forNew() { return new PropertyId(null); }

    public boolean isNew() {
        return value == null;
    }
}

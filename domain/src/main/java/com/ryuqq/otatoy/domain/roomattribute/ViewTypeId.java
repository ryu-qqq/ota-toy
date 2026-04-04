package com.ryuqq.otatoy.domain.roomattribute;

/**
 * 전망 유형 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record ViewTypeId(Long value) {

    public static ViewTypeId of(Long value) {
        return new ViewTypeId(value);
    }

    public static ViewTypeId forNew() { return new ViewTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.roomattribute;

/**
 * 침대 유형 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record BedTypeId(Long value) {

    public static BedTypeId of(Long value) {
        return new BedTypeId(value);
    }

    public static BedTypeId forNew() { return new BedTypeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

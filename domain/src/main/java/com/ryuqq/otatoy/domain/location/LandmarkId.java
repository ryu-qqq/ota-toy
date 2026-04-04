package com.ryuqq.otatoy.domain.location;

/**
 * 랜드마크 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record LandmarkId(Long value) {

    public static LandmarkId of(Long value) {
        return new LandmarkId(value);
    }

    public static LandmarkId forNew() { return new LandmarkId(null); }

    public boolean isNew() {
        return value == null;
    }
}

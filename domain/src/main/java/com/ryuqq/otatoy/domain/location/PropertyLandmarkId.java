package com.ryuqq.otatoy.domain.location;

/**
 * 숙소-랜드마크 매핑 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PropertyLandmarkId(Long value) {

    public static PropertyLandmarkId of(Long value) {
        return new PropertyLandmarkId(value);
    }

    public static PropertyLandmarkId forNew() { return new PropertyLandmarkId(null); }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 편의시설 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record PropertyAmenityId(Long value) {

    public static PropertyAmenityId of(Long value) {
        return new PropertyAmenityId(value);
    }

    public static PropertyAmenityId forNew() { return new PropertyAmenityId(null); }

    public boolean isNew() {
        return value == null;
    }
}

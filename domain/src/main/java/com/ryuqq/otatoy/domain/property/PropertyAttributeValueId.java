package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 속성값 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record PropertyAttributeValueId(Long value) {

    public static PropertyAttributeValueId of(Long value) {
        return new PropertyAttributeValueId(value);
    }

    public static PropertyAttributeValueId forNew() { return new PropertyAttributeValueId(null); }

    public boolean isNew() {
        return value == null;
    }
}

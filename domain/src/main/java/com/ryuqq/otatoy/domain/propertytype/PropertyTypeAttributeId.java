package com.ryuqq.otatoy.domain.propertytype;

/**
 * 숙소 유형 속성 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record PropertyTypeAttributeId(Long value) {

    public static PropertyTypeAttributeId of(Long value) {
        return new PropertyTypeAttributeId(value);
    }

    public static PropertyTypeAttributeId forNew() { return new PropertyTypeAttributeId(null); }

    public boolean isNew() {
        return value == null;
    }
}

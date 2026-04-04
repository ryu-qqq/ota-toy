package com.ryuqq.otatoy.domain.accommodation;

public record PropertyAttributeValue(
        Long id,
        PropertyId propertyId,
        Long propertyTypeAttributeId,
        String value
) {

    public PropertyAttributeValue {
        if (propertyId == null) {
            throw new IllegalArgumentException("숙소 ID는 필수입니다");
        }
        if (propertyTypeAttributeId == null) {
            throw new IllegalArgumentException("숙소 유형 속성 ID는 필수입니다");
        }
    }

    public static PropertyAttributeValue of(PropertyId propertyId, Long propertyTypeAttributeId, String value) {
        return new PropertyAttributeValue(null, propertyId, propertyTypeAttributeId, value);
    }

    public static PropertyAttributeValue reconstitute(Long id, PropertyId propertyId,
                                                       Long propertyTypeAttributeId, String value) {
        return new PropertyAttributeValue(id, propertyId, propertyTypeAttributeId, value);
    }
}

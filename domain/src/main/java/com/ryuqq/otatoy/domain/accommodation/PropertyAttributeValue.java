package com.ryuqq.otatoy.domain.accommodation;

public record PropertyAttributeValue(
        Long id,
        PropertyId propertyId,
        Long propertyTypeAttributeId,
        String value
) {

    public static PropertyAttributeValue of(PropertyId propertyId, Long propertyTypeAttributeId, String value) {
        return new PropertyAttributeValue(null, propertyId, propertyTypeAttributeId, value);
    }

    public static PropertyAttributeValue reconstitute(Long id, PropertyId propertyId,
                                                       Long propertyTypeAttributeId, String value) {
        return new PropertyAttributeValue(id, propertyId, propertyTypeAttributeId, value);
    }
}

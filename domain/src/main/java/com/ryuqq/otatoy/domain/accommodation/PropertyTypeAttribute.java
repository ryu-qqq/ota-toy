package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeAttribute(
        Long id,
        Long propertyTypeId,
        String attributeKey,
        String attributeName,
        String valueType,
        boolean required,
        int sortOrder
) {

    public static PropertyTypeAttribute of(Long propertyTypeId, String attributeKey, String attributeName,
                                            String valueType, boolean required, int sortOrder) {
        return new PropertyTypeAttribute(null, propertyTypeId, attributeKey, attributeName,
                valueType, required, sortOrder);
    }

    public static PropertyTypeAttribute reconstitute(Long id, Long propertyTypeId, String attributeKey,
                                                      String attributeName, String valueType,
                                                      boolean required, int sortOrder) {
        return new PropertyTypeAttribute(id, propertyTypeId, attributeKey, attributeName,
                valueType, required, sortOrder);
    }
}

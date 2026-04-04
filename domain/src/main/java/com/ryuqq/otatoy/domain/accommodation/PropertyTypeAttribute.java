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

    public PropertyTypeAttribute {
        if (attributeKey == null || attributeKey.isBlank()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
        if (attributeName == null || attributeName.isBlank()) {
            throw new IllegalArgumentException("속성명은 필수입니다");
        }
        if (valueType == null || valueType.isBlank()) {
            throw new IllegalArgumentException("값 유형은 필수입니다");
        }
    }

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

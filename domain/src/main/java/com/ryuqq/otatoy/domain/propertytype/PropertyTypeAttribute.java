package com.ryuqq.otatoy.domain.propertytype;

import java.util.Objects;

public class PropertyTypeAttribute {

    private final PropertyTypeAttributeId id;
    private final PropertyTypeId propertyTypeId;
    private final String attributeKey;
    private final String attributeName;
    private final String valueType;
    private final boolean required;
    private final int sortOrder;

    private PropertyTypeAttribute(PropertyTypeAttributeId id, PropertyTypeId propertyTypeId, String attributeKey,
                                   String attributeName, String valueType,
                                   boolean required, int sortOrder) {
        this.id = id;
        this.propertyTypeId = propertyTypeId;
        this.attributeKey = attributeKey;
        this.attributeName = attributeName;
        this.valueType = valueType;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    public static PropertyTypeAttribute forNew(PropertyTypeId propertyTypeId, String attributeKey, String attributeName,
                                                String valueType, boolean required, int sortOrder) {
        validate(attributeKey, attributeName, valueType);
        return new PropertyTypeAttribute(PropertyTypeAttributeId.of(null), propertyTypeId, attributeKey, attributeName,
                valueType, required, sortOrder);
    }

    private static void validate(String attributeKey, String attributeName, String valueType) {
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

    public static PropertyTypeAttribute reconstitute(PropertyTypeAttributeId id, PropertyTypeId propertyTypeId, String attributeKey,
                                                      String attributeName, String valueType,
                                                      boolean required, int sortOrder) {
        return new PropertyTypeAttribute(id, propertyTypeId, attributeKey, attributeName,
                valueType, required, sortOrder);
    }

    public PropertyTypeAttributeId id() { return id; }
    public PropertyTypeId propertyTypeId() { return propertyTypeId; }
    public String attributeKey() { return attributeKey; }
    public String attributeName() { return attributeName; }
    public String valueType() { return valueType; }
    public boolean required() { return required; }
    public int sortOrder() { return sortOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyTypeAttribute p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

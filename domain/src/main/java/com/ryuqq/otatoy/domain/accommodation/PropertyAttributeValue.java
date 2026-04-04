package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class PropertyAttributeValue {

    private final PropertyAttributeValueId id;
    private final PropertyId propertyId;
    private final PropertyTypeAttributeId propertyTypeAttributeId;
    private final String value;

    private PropertyAttributeValue(PropertyAttributeValueId id, PropertyId propertyId,
                                    PropertyTypeAttributeId propertyTypeAttributeId, String value) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyTypeAttributeId = propertyTypeAttributeId;
        this.value = value;
    }

    public static PropertyAttributeValue forNew(PropertyId propertyId, PropertyTypeAttributeId propertyTypeAttributeId, String value) {
        if (propertyId == null) {
            throw new IllegalArgumentException("숙소 ID는 필수입니다");
        }
        if (propertyTypeAttributeId == null || propertyTypeAttributeId.value() == null) {
            throw new IllegalArgumentException("숙소 유형 속성 ID는 필수입니다");
        }
        return new PropertyAttributeValue(PropertyAttributeValueId.of(null), propertyId, propertyTypeAttributeId, value);
    }

    public static PropertyAttributeValue reconstitute(PropertyAttributeValueId id, PropertyId propertyId,
                                                       PropertyTypeAttributeId propertyTypeAttributeId, String value) {
        return new PropertyAttributeValue(id, propertyId, propertyTypeAttributeId, value);
    }

    public PropertyAttributeValueId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PropertyTypeAttributeId propertyTypeAttributeId() { return propertyTypeAttributeId; }
    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAttributeValue p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

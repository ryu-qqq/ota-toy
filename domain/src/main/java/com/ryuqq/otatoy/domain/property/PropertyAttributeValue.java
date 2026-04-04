package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import java.time.Instant;
import java.util.Objects;

/**
 * 숙소의 EAV(Entity-Attribute-Value) 패턴 속성값을 나타내는 엔티티.
 * PropertyType에 정의된 속성에 대한 실제 값을 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PropertyAttributeValue {

    private final PropertyAttributeValueId id;
    private final PropertyId propertyId;
    private final PropertyTypeAttributeId propertyTypeAttributeId;
    private final String value;
    private final Instant createdAt;
    private Instant updatedAt;

    private PropertyAttributeValue(PropertyAttributeValueId id, PropertyId propertyId,
                                    PropertyTypeAttributeId propertyTypeAttributeId, String value,
                                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyTypeAttributeId = propertyTypeAttributeId;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PropertyAttributeValue forNew(PropertyId propertyId, PropertyTypeAttributeId propertyTypeAttributeId,
                                                 String value, Instant now) {
        validate(propertyId, propertyTypeAttributeId);
        return new PropertyAttributeValue(PropertyAttributeValueId.of(null), propertyId, propertyTypeAttributeId, value, now, now);
    }

    private static void validate(PropertyId propertyId, PropertyTypeAttributeId propertyTypeAttributeId) {
        if (propertyId == null) {
            throw new IllegalArgumentException("숙소 ID는 필수입니다");
        }
        if (propertyTypeAttributeId == null || propertyTypeAttributeId.value() == null) {
            throw new IllegalArgumentException("숙소 유형 속성 ID는 필수입니다");
        }
    }

    public static PropertyAttributeValue reconstitute(PropertyAttributeValueId id, PropertyId propertyId,
                                                       PropertyTypeAttributeId propertyTypeAttributeId, String value,
                                                       Instant createdAt, Instant updatedAt) {
        return new PropertyAttributeValue(id, propertyId, propertyTypeAttributeId, value, createdAt, updatedAt);
    }

    public PropertyAttributeValueId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PropertyTypeAttributeId propertyTypeAttributeId() { return propertyTypeAttributeId; }
    public String value() { return value; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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

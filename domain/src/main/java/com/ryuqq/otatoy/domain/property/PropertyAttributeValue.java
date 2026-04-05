package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
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
    private DeletionStatus deletionStatus;

    private PropertyAttributeValue(PropertyAttributeValueId id, PropertyId propertyId,
                                    PropertyTypeAttributeId propertyTypeAttributeId, String value,
                                    Instant createdAt, Instant updatedAt, DeletionStatus deletionStatus) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyTypeAttributeId = propertyTypeAttributeId;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletionStatus = deletionStatus;
    }

    public static PropertyAttributeValue forNew(PropertyId propertyId, PropertyTypeAttributeId propertyTypeAttributeId,
                                                 String value, Instant now) {
        validate(propertyId, propertyTypeAttributeId);
        return new PropertyAttributeValue(PropertyAttributeValueId.of(null), propertyId, propertyTypeAttributeId,
                value, now, now, DeletionStatus.active());
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
                                                       Instant createdAt, Instant updatedAt,
                                                       DeletionStatus deletionStatus) {
        return new PropertyAttributeValue(id, propertyId, propertyTypeAttributeId, value,
                createdAt, updatedAt, deletionStatus);
    }

    /**
     * soft delete 처리한다. 이미 삭제된 상태이면 무시한다.
     */
    public void delete(Instant now) {
        if (!deletionStatus.deleted()) {
            this.deletionStatus = DeletionStatus.deleted(now);
            this.updatedAt = now;
        }
    }

    /**
     * diff 비교를 위한 비즈니스 키. propertyTypeAttributeId로 동일 속성을 식별한다.
     */
    public Long attributeKey() {
        return propertyTypeAttributeId.value();
    }

    public boolean isDeleted() {
        return deletionStatus.deleted();
    }

    public PropertyAttributeValueId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PropertyTypeAttributeId propertyTypeAttributeId() { return propertyTypeAttributeId; }
    public String value() { return value; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public DeletionStatus deletionStatus() { return deletionStatus; }

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

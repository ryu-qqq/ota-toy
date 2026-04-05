package com.ryuqq.otatoy.persistence.propertytype.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * PropertyTypeAttribute JPA Entity.
 * 숙소 유형별 속성 정의를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property_type_attribute")
public class PropertyTypeAttributeJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long propertyTypeId;
    private String attributeKey;
    private String attributeName;
    private String valueType;
    @Column(name = "is_required")
    private boolean required;
    private int sortOrder;

    protected PropertyTypeAttributeJpaEntity() {
        super();
    }

    private PropertyTypeAttributeJpaEntity(Long id, Long propertyTypeId, String attributeKey,
                                            String attributeName, String valueType,
                                            boolean required, int sortOrder,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyTypeId = propertyTypeId;
        this.attributeKey = attributeKey;
        this.attributeName = attributeName;
        this.valueType = valueType;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    public static PropertyTypeAttributeJpaEntity create(Long id, Long propertyTypeId, String attributeKey,
                                                         String attributeName, String valueType,
                                                         boolean required, int sortOrder,
                                                         Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyTypeAttributeJpaEntity(id, propertyTypeId, attributeKey,
                attributeName, valueType, required, sortOrder, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPropertyTypeId() { return propertyTypeId; }
    public String getAttributeKey() { return attributeKey; }
    public String getAttributeName() { return attributeName; }
    public String getValueType() { return valueType; }
    public boolean isRequired() { return required; }
    public int getSortOrder() { return sortOrder; }
}

package com.ryuqq.otatoy.persistence.property.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * PropertyAttributeValue JPA Entity.
 * 숙소 EAV 속성값을 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property_attribute_value")
public class PropertyAttributeValueJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private Long propertyTypeAttributeId;

    @Column(nullable = false, length = 500)
    private String value;

    protected PropertyAttributeValueJpaEntity() {
        super();
    }

    private PropertyAttributeValueJpaEntity(Long id, Long propertyId, Long propertyTypeAttributeId,
                                             String value,
                                             Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyId = propertyId;
        this.propertyTypeAttributeId = propertyTypeAttributeId;
        this.value = value;
    }

    public static PropertyAttributeValueJpaEntity create(Long id, Long propertyId, Long propertyTypeAttributeId,
                                                          String value,
                                                          Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyAttributeValueJpaEntity(id, propertyId, propertyTypeAttributeId,
                value, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getPropertyTypeAttributeId() { return propertyTypeAttributeId; }
    public String getValue() { return value; }
}

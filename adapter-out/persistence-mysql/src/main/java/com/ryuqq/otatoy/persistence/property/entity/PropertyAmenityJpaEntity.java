package com.ryuqq.otatoy.persistence.property.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PropertyAmenity JPA Entity.
 * 숙소 편의시설을 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property_amenity")
public class PropertyAmenityJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long propertyId;
    private String amenityType;
    private String name;
    private BigDecimal additionalPrice;
    private int sortOrder;

    protected PropertyAmenityJpaEntity() {
        super();
    }

    private PropertyAmenityJpaEntity(Long id, Long propertyId, String amenityType,
                                      String name, BigDecimal additionalPrice, int sortOrder,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyId = propertyId;
        this.amenityType = amenityType;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sortOrder = sortOrder;
    }

    public static PropertyAmenityJpaEntity create(Long id, Long propertyId, String amenityType,
                                                    String name, BigDecimal additionalPrice, int sortOrder,
                                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyAmenityJpaEntity(id, propertyId, amenityType,
                name, additionalPrice, sortOrder, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getAmenityType() { return amenityType; }
    public String getName() { return name; }
    public BigDecimal getAdditionalPrice() { return additionalPrice; }
    public int getSortOrder() { return sortOrder; }
}

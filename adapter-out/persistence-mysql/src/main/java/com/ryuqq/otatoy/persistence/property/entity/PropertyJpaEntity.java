package com.ryuqq.otatoy.persistence.property.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Property JPA Entity.
 * 숙소 기본 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property")
public class PropertyJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long partnerId;
    private Long brandId;
    private Long propertyTypeId;
    private String name;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String neighborhood;
    private String region;
    private String status;
    private String promotionText;

    protected PropertyJpaEntity() {
        super();
    }

    private PropertyJpaEntity(Long id, Long partnerId, Long brandId, Long propertyTypeId,
                               String name, String description, String address,
                               double latitude, double longitude,
                               String neighborhood, String region,
                               String status, String promotionText,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.partnerId = partnerId;
        this.brandId = brandId;
        this.propertyTypeId = propertyTypeId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighborhood = neighborhood;
        this.region = region;
        this.status = status;
        this.promotionText = promotionText;
    }

    public static PropertyJpaEntity create(Long id, Long partnerId, Long brandId, Long propertyTypeId,
                                            String name, String description, String address,
                                            double latitude, double longitude,
                                            String neighborhood, String region,
                                            String status, String promotionText,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyJpaEntity(id, partnerId, brandId, propertyTypeId,
                name, description, address, latitude, longitude,
                neighborhood, region, status, promotionText,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPartnerId() { return partnerId; }
    public Long getBrandId() { return brandId; }
    public Long getPropertyTypeId() { return propertyTypeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getNeighborhood() { return neighborhood; }
    public String getRegion() { return region; }
    public String getStatus() { return status; }
    public String getPromotionText() { return promotionText; }
}

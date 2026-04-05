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
 * PropertyPhoto JPA Entity.
 * 숙소 사진을 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property_photo")
public class PropertyPhotoJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 50)
    private String photoType;

    @Column(nullable = false, length = 1000)
    private String originUrl;

    @Column(length = 1000)
    private String cdnUrl;

    @Column(nullable = false)
    private int sortOrder;

    protected PropertyPhotoJpaEntity() {
        super();
    }

    private PropertyPhotoJpaEntity(Long id, Long propertyId, String photoType,
                                    String originUrl, String cdnUrl, int sortOrder,
                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyId = propertyId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
    }

    public static PropertyPhotoJpaEntity create(Long id, Long propertyId, String photoType,
                                                 String originUrl, String cdnUrl, int sortOrder,
                                                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyPhotoJpaEntity(id, propertyId, photoType,
                originUrl, cdnUrl, sortOrder, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getPhotoType() { return photoType; }
    public String getOriginUrl() { return originUrl; }
    public String getCdnUrl() { return cdnUrl; }
    public int getSortOrder() { return sortOrder; }
}

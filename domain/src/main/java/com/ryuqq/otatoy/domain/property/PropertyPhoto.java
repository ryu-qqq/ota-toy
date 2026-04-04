package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.time.Instant;
import java.util.Objects;

/**
 * 숙소에 연결된 개별 사진을 나타내는 엔티티.
 * 사진 유형, 원본 URL, CDN URL, 정렬 순서를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PropertyPhoto {

    private final PropertyPhotoId id;
    private final PropertyId propertyId;
    private final PhotoType photoType;
    private final OriginUrl originUrl;
    private final CdnUrl cdnUrl;
    private final int sortOrder;
    private final Instant createdAt;
    private Instant updatedAt;

    private PropertyPhoto(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                          OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PropertyPhoto forNew(PropertyId propertyId, PhotoType photoType,
                                        OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder, Instant now) {
        return new PropertyPhoto(PropertyPhotoId.of(null), propertyId, photoType, originUrl, cdnUrl, sortOrder, now, now);
    }

    public static PropertyPhoto reconstitute(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                                              OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                                              Instant createdAt, Instant updatedAt) {
        return new PropertyPhoto(id, propertyId, photoType, originUrl, cdnUrl, sortOrder, createdAt, updatedAt);
    }

    public PropertyPhotoId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PhotoType photoType() { return photoType; }
    public OriginUrl originUrl() { return originUrl; }
    public CdnUrl cdnUrl() { return cdnUrl; }
    public int sortOrder() { return sortOrder; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyPhoto p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

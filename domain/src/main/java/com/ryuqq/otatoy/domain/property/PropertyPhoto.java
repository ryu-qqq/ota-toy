package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.util.Objects;

/**
 * 숙소에 연결된 개별 사진을 나타내는 엔티티.
 * 사진 유형, 원본 URL, CDN URL, 정렬 순서를 관리한다.
 */
public class PropertyPhoto {

    private final PropertyPhotoId id;
    private final PropertyId propertyId;
    private final PhotoType photoType;
    private final OriginUrl originUrl;
    private final CdnUrl cdnUrl;
    private final int sortOrder;

    private PropertyPhoto(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                          OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        this.id = id;
        this.propertyId = propertyId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
    }

    public static PropertyPhoto forNew(PropertyId propertyId, PhotoType photoType,
                                        OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        return new PropertyPhoto(PropertyPhotoId.of(null), propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public static PropertyPhoto reconstitute(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                                              OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        return new PropertyPhoto(id, propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public PropertyPhotoId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PhotoType photoType() { return photoType; }
    public OriginUrl originUrl() { return originUrl; }
    public CdnUrl cdnUrl() { return cdnUrl; }
    public int sortOrder() { return sortOrder; }

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

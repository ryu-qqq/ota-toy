package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class PropertyPhoto {

    private final PropertyPhotoId id;
    private final PropertyId propertyId;
    private final PhotoType photoType;
    private final String originUrl;
    private final String cdnUrl;
    private final int sortOrder;

    private PropertyPhoto(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                          String originUrl, String cdnUrl, int sortOrder) {
        this.id = id;
        this.propertyId = propertyId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
    }

    public static PropertyPhoto forNew(PropertyId propertyId, PhotoType photoType,
                                        String originUrl, String cdnUrl, int sortOrder) {
        if (originUrl == null || originUrl.isBlank()) {
            throw new IllegalArgumentException("원본 이미지 URL은 필수입니다");
        }
        return new PropertyPhoto(PropertyPhotoId.of(null), propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public static PropertyPhoto reconstitute(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                                              String originUrl, String cdnUrl, int sortOrder) {
        return new PropertyPhoto(id, propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public PropertyPhotoId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PhotoType photoType() { return photoType; }
    public String originUrl() { return originUrl; }
    public String cdnUrl() { return cdnUrl; }
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

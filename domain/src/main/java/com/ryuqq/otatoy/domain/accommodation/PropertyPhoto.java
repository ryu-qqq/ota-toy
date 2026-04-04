package com.ryuqq.otatoy.domain.accommodation;

public record PropertyPhoto(
        Long id,
        PropertyId propertyId,
        String photoType,
        String originUrl,
        String cdnUrl,
        int sortOrder
) {

    public PropertyPhoto {
        if (originUrl == null || originUrl.isBlank()) {
            throw new IllegalArgumentException("원본 이미지 URL은 필수입니다");
        }
    }

    public static PropertyPhoto of(PropertyId propertyId, String photoType,
                                    String originUrl, String cdnUrl, int sortOrder) {
        return new PropertyPhoto(null, propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public static PropertyPhoto reconstitute(Long id, PropertyId propertyId, String photoType,
                                              String originUrl, String cdnUrl, int sortOrder) {
        return new PropertyPhoto(id, propertyId, photoType, originUrl, cdnUrl, sortOrder);
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record RoomPhoto(
        Long id,
        RoomTypeId roomTypeId,
        String photoType,
        String originUrl,
        String cdnUrl,
        int sortOrder
) {

    public RoomPhoto {
        if (originUrl == null || originUrl.isBlank()) {
            throw new IllegalArgumentException("원본 이미지 URL은 필수입니다");
        }
    }

    public static RoomPhoto of(RoomTypeId roomTypeId, String photoType,
                                String originUrl, String cdnUrl, int sortOrder) {
        return new RoomPhoto(null, roomTypeId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public static RoomPhoto reconstitute(Long id, RoomTypeId roomTypeId, String photoType,
                                          String originUrl, String cdnUrl, int sortOrder) {
        return new RoomPhoto(id, roomTypeId, photoType, originUrl, cdnUrl, sortOrder);
    }
}

package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.time.Instant;
import java.util.Objects;

/**
 * 객실에 연결된 개별 사진을 나타내는 엔티티.
 * 사진 유형, 원본 URL, CDN URL, 정렬 순서를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class RoomPhoto {

    private final RoomPhotoId id;
    private final RoomTypeId roomTypeId;
    private final PhotoType photoType;
    private final OriginUrl originUrl;
    private final CdnUrl cdnUrl;
    private final int sortOrder;
    private final Instant createdAt;
    private Instant updatedAt;

    private RoomPhoto(RoomPhotoId id, RoomTypeId roomTypeId, PhotoType photoType,
                      OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                      Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RoomPhoto forNew(RoomTypeId roomTypeId, PhotoType photoType,
                                    OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder, Instant now) {
        return new RoomPhoto(RoomPhotoId.of(null), roomTypeId, photoType, originUrl, cdnUrl, sortOrder, now, now);
    }

    public static RoomPhoto reconstitute(RoomPhotoId id, RoomTypeId roomTypeId, PhotoType photoType,
                                          OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                                          Instant createdAt, Instant updatedAt) {
        return new RoomPhoto(id, roomTypeId, photoType, originUrl, cdnUrl, sortOrder, createdAt, updatedAt);
    }

    public RoomPhotoId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public PhotoType photoType() { return photoType; }
    public OriginUrl originUrl() { return originUrl; }
    public CdnUrl cdnUrl() { return cdnUrl; }
    public int sortOrder() { return sortOrder; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomPhoto r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

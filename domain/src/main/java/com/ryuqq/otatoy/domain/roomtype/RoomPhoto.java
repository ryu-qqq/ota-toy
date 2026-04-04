package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.util.Objects;

public class RoomPhoto {

    private final RoomPhotoId id;
    private final RoomTypeId roomTypeId;
    private final PhotoType photoType;
    private final OriginUrl originUrl;
    private final CdnUrl cdnUrl;
    private final int sortOrder;

    private RoomPhoto(RoomPhotoId id, RoomTypeId roomTypeId, PhotoType photoType,
                      OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
    }

    public static RoomPhoto forNew(RoomTypeId roomTypeId, PhotoType photoType,
                                    OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        return new RoomPhoto(RoomPhotoId.of(null), roomTypeId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public static RoomPhoto reconstitute(RoomPhotoId id, RoomTypeId roomTypeId, PhotoType photoType,
                                          OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
        return new RoomPhoto(id, roomTypeId, photoType, originUrl, cdnUrl, sortOrder);
    }

    public RoomPhotoId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public PhotoType photoType() { return photoType; }
    public OriginUrl originUrl() { return originUrl; }
    public CdnUrl cdnUrl() { return cdnUrl; }
    public int sortOrder() { return sortOrder; }

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

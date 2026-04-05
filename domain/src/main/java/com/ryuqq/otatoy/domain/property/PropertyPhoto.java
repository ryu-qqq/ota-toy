package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.time.Instant;
import java.util.Objects;

/**
 * 숙소에 연결된 개별 사진을 나타내는 엔티티.
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
    private DeletionStatus deletionStatus;

    private PropertyPhoto(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                          OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                          Instant createdAt, Instant updatedAt, DeletionStatus deletionStatus) {
        this.id = id;
        this.propertyId = propertyId;
        this.photoType = photoType;
        this.originUrl = originUrl;
        this.cdnUrl = cdnUrl;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletionStatus = deletionStatus;
    }

    public static PropertyPhoto forNew(PropertyId propertyId, PhotoType photoType,
                                        OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder, Instant now) {
        return new PropertyPhoto(PropertyPhotoId.of(null), propertyId, photoType, originUrl, cdnUrl,
                sortOrder, now, now, DeletionStatus.active());
    }

    public static PropertyPhoto reconstitute(PropertyPhotoId id, PropertyId propertyId, PhotoType photoType,
                                              OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder,
                                              Instant createdAt, Instant updatedAt, DeletionStatus deletionStatus) {
        return new PropertyPhoto(id, propertyId, photoType, originUrl, cdnUrl, sortOrder,
                createdAt, updatedAt, deletionStatus);
    }

    public void delete(Instant now) {
        if (!deletionStatus.deleted()) {
            this.deletionStatus = DeletionStatus.deleted(now);
            this.updatedAt = now;
        }
    }

    /**
     * diff 비교를 위한 비즈니스 키. originUrl + photoType 조합으로 동일 사진을 식별한다.
     */
    public String photoKey() {
        return originUrl.value() + "::" + photoType.name();
    }

    public boolean isDeleted() {
        return deletionStatus.deleted();
    }

    public PropertyPhotoId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public PhotoType photoType() { return photoType; }
    public OriginUrl originUrl() { return originUrl; }
    public CdnUrl cdnUrl() { return cdnUrl; }
    public int sortOrder() { return sortOrder; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public DeletionStatus deletionStatus() { return deletionStatus; }

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

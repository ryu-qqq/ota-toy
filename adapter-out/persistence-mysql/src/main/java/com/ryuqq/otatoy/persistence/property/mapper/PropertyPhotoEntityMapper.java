package com.ryuqq.otatoy.persistence.property.mapper;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.domain.property.PropertyPhotoId;
import com.ryuqq.otatoy.persistence.property.entity.PropertyPhotoJpaEntity;
import org.springframework.stereotype.Component;

/**
 * PropertyPhoto Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyPhotoEntityMapper {

    public PropertyPhotoJpaEntity toEntity(PropertyPhoto domain) {
        return PropertyPhotoJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.propertyId().value(),
                domain.photoType().name(),
                domain.originUrl().value(),
                domain.cdnUrl() != null ? domain.cdnUrl().value() : null,
                domain.sortOrder(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.deletionStatus().deletedAt()
        );
    }

    public PropertyPhoto toDomain(PropertyPhotoJpaEntity entity) {
        DeletionStatus deletionStatus = entity.isDeleted()
                ? DeletionStatus.deleted(entity.getDeletedAt())
                : DeletionStatus.active();

        return PropertyPhoto.reconstitute(
                PropertyPhotoId.of(entity.getId()),
                PropertyId.of(entity.getPropertyId()),
                PhotoType.valueOf(entity.getPhotoType()),
                OriginUrl.of(entity.getOriginUrl()),
                entity.getCdnUrl() != null ? CdnUrl.of(entity.getCdnUrl()) : null,
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                deletionStatus
        );
    }
}

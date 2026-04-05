package com.ryuqq.otatoy.persistence.property.mapper;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyAmenityId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAmenityJpaEntity;
import org.springframework.stereotype.Component;

/**
 * PropertyAmenity Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityEntityMapper {

    public PropertyAmenityJpaEntity toEntity(PropertyAmenity domain) {
        return PropertyAmenityJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.propertyId().value(),
                domain.amenityType().name(),
                domain.name().value(),
                domain.additionalPrice() != null ? domain.additionalPrice().amount() : null,
                domain.sortOrder(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.deletionStatus().deleted() ? domain.deletionStatus().deletedAt() : null
        );
    }

    public PropertyAmenity toDomain(PropertyAmenityJpaEntity entity) {
        DeletionStatus deletionStatus = entity.isDeleted()
                ? DeletionStatus.deleted(entity.getDeletedAt())
                : DeletionStatus.active();

        return PropertyAmenity.reconstitute(
                PropertyAmenityId.of(entity.getId()),
                PropertyId.of(entity.getPropertyId()),
                AmenityType.valueOf(entity.getAmenityType()),
                AmenityName.of(entity.getName()),
                entity.getAdditionalPrice() != null ? Money.of(entity.getAdditionalPrice()) : null,
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                deletionStatus
        );
    }
}

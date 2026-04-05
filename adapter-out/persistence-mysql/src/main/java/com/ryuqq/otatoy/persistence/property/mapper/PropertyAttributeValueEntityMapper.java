package com.ryuqq.otatoy.persistence.property.mapper;

import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValueId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAttributeValueJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * PropertyAttributeValue Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueEntityMapper {

    public PropertyAttributeValueJpaEntity toEntity(PropertyAttributeValue domain) {
        Instant now = Instant.now();
        return PropertyAttributeValueJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.propertyId().value(),
                domain.propertyTypeAttributeId().value(),
                domain.value(),
                domain.createdAt() != null ? domain.createdAt() : now,
                now,
                domain.deletionStatus().deleted() ? domain.deletionStatus().deletedAt() : null
        );
    }

    public PropertyAttributeValue toDomain(PropertyAttributeValueJpaEntity entity) {
        DeletionStatus deletionStatus = entity.isDeleted()
                ? DeletionStatus.deleted(entity.getDeletedAt())
                : DeletionStatus.active();

        return PropertyAttributeValue.reconstitute(
                PropertyAttributeValueId.of(entity.getId()),
                PropertyId.of(entity.getPropertyId()),
                PropertyTypeAttributeId.of(entity.getPropertyTypeAttributeId()),
                entity.getValue(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                deletionStatus
        );
    }
}

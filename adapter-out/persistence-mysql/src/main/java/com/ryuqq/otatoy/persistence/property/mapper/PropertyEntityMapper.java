package com.ryuqq.otatoy.persistence.property.mapper;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PromotionText;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Property Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public PropertyJpaEntity toEntity(Property domain) {
        return PropertyJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.partnerId().value(),
                domain.brandId() != null ? domain.brandId().value() : null,
                domain.propertyTypeId().value(),
                domain.name().value(),
                domain.description() != null ? domain.description().value() : null,
                domain.location().address(),
                domain.location().latitude(),
                domain.location().longitude(),
                domain.location().neighborhood(),
                domain.location().region(),
                domain.status().name(),
                domain.promotionText() != null ? domain.promotionText().value() : null,
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public Property toDomain(PropertyJpaEntity entity) {
        return Property.reconstitute(
                PropertyId.of(entity.getId()),
                PartnerId.of(entity.getPartnerId()),
                entity.getBrandId() != null ? BrandId.of(entity.getBrandId()) : null,
                PropertyTypeId.of(entity.getPropertyTypeId()),
                PropertyName.of(entity.getName()),
                entity.getDescription() != null ? PropertyDescription.of(entity.getDescription()) : null,
                Location.of(entity.getAddress(), entity.getLatitude(), entity.getLongitude(),
                        entity.getNeighborhood(), entity.getRegion()),
                entity.getPromotionText() != null ? PromotionText.of(entity.getPromotionText()) : null,
                PropertyStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

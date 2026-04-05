package com.ryuqq.otatoy.persistence.propertytype.mapper;

import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeCode;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeDescription;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeName;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeAttributeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeJpaEntity;
import org.springframework.stereotype.Component;

/**
 * PropertyType Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyTypeEntityMapper {

    public PropertyType toDomain(PropertyTypeJpaEntity entity) {
        return PropertyType.reconstitute(
                PropertyTypeId.of(entity.getId()),
                PropertyTypeCode.of(entity.getCode()),
                PropertyTypeName.of(entity.getName()),
                entity.getDescription() != null ? PropertyTypeDescription.of(entity.getDescription()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PropertyTypeAttribute toAttributeDomain(PropertyTypeAttributeJpaEntity entity) {
        return PropertyTypeAttribute.reconstitute(
                PropertyTypeAttributeId.of(entity.getId()),
                PropertyTypeId.of(entity.getPropertyTypeId()),
                entity.getAttributeKey(),
                entity.getAttributeName(),
                entity.getValueType(),
                entity.isRequired(),
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

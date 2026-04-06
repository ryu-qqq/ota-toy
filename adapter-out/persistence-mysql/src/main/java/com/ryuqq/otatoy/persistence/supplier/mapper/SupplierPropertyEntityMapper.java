package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.supplier.SupplierMappingStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierPropertyId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierPropertyJpaEntity;
import org.springframework.stereotype.Component;

/**
 * SupplierProperty Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierPropertyEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시).
     */
    public SupplierPropertyJpaEntity toEntity(SupplierProperty domain) {
        return SupplierPropertyJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.supplierId().value(),
                domain.propertyId().value(),
                domain.supplierPropertyCode(),
                domain.lastSyncedAt(),
                domain.status().name(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시).
     */
    public SupplierProperty toDomain(SupplierPropertyJpaEntity entity) {
        return SupplierProperty.reconstitute(
                SupplierPropertyId.of(entity.getId()),
                SupplierId.of(entity.getSupplierId()),
                PropertyId.of(entity.getPropertyId()),
                entity.getSupplierPropertyCode(),
                entity.getLastSyncedAt(),
                SupplierMappingStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

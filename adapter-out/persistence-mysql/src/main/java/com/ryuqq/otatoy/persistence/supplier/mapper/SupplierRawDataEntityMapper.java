package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataId;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierRawDataJpaEntity;
import org.springframework.stereotype.Component;

/**
 * SupplierRawData Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시).
     */
    public SupplierRawDataJpaEntity toEntity(SupplierRawData domain) {
        return SupplierRawDataJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.supplierId().value(),
                domain.rawPayload(),
                domain.status().name(),
                domain.fetchedAt(),
                domain.processedAt(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시).
     */
    public SupplierRawData toDomain(SupplierRawDataJpaEntity entity) {
        return SupplierRawData.reconstitute(
                SupplierRawDataId.of(entity.getId()),
                SupplierId.of(entity.getSupplierId()),
                entity.getRawPayload(),
                SupplierRawDataStatus.valueOf(entity.getStatus()),
                entity.getFetchedAt(),
                entity.getProcessedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

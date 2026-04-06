package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLogId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierSyncLogJpaEntity;
import org.springframework.stereotype.Component;

/**
 * SupplierSyncLog Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncLogEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시).
     */
    public SupplierSyncLogJpaEntity toEntity(SupplierSyncLog domain) {
        return SupplierSyncLogJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.supplierId().value(),
                domain.syncType().name(),
                domain.syncedAt(),
                domain.status().name(),
                domain.totalCount(),
                domain.createdCount(),
                domain.updatedCount(),
                domain.deletedCount(),
                domain.errorMessage(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시).
     */
    public SupplierSyncLog toDomain(SupplierSyncLogJpaEntity entity) {
        return SupplierSyncLog.reconstitute(
                SupplierSyncLogId.of(entity.getId()),
                SupplierId.of(entity.getSupplierId()),
                SupplierSyncType.valueOf(entity.getSyncType()),
                entity.getSyncedAt(),
                SupplierSyncStatus.valueOf(entity.getStatus()),
                entity.getTotalCount(),
                entity.getCreatedCount(),
                entity.getUpdatedCount(),
                entity.getDeletedCount(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

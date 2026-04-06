package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskId;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierTaskJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SupplierTaskEntityMapper {

    public SupplierTaskJpaEntity toEntity(SupplierTask domain) {
        return SupplierTaskJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.supplierId().value(),
                domain.supplierApiConfigId(),
                domain.taskType().name(),
                domain.status().name(),
                domain.payload(),
                domain.retryCount(),
                domain.maxRetries(),
                domain.failureReason(),
                domain.processedAt(),
                domain.createdAt(),
                domain.createdAt()
        );
    }

    public SupplierTask toDomain(SupplierTaskJpaEntity entity) {
        return SupplierTask.reconstitute(
                SupplierTaskId.of(entity.getId()),
                SupplierId.of(entity.getSupplierId()),
                entity.getSupplierApiConfigId(),
                SupplierTaskType.valueOf(entity.getTaskType()),
                SupplierTaskStatus.valueOf(entity.getStatus()),
                entity.getPayload(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}

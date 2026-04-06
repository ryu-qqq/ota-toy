package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierApiConfigJpaEntity;
import org.springframework.stereotype.Component;

/**
 * SupplierApiConfig Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierApiConfigEntityMapper {

    public SupplierApiConfig toDomain(SupplierApiConfigJpaEntity entity) {
        return SupplierApiConfig.reconstitute(
                entity.getId(),
                SupplierId.of(entity.getSupplierId()),
                SupplierApiType.valueOf(entity.getApiType()),
                entity.getApiBaseUrl(),
                entity.getApiKey(),
                entity.getAuthType(),
                entity.getSyncIntervalMinutes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

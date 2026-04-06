package com.ryuqq.otatoy.application.supplier.facade;

import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.port.out.PropertyCommandPort;
import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierPropertyCommandPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogCommandPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공급자 Property 동기화 저장 Facade.
 * Diff 결과를 받아서 저장만 한다 — 변환/상태 변경 로직 없음.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncPersistenceFacade {

    private final PropertyCommandPort propertyCommandPort;
    private final SupplierPropertyCommandPort supplierPropertyCommandPort;
    private final SupplierSyncLogCommandPort supplierSyncLogCommandPort;
    private final PropertyFactory propertyFactory;

    public SupplierSyncPersistenceFacade(PropertyCommandPort propertyCommandPort,
                                          SupplierPropertyCommandPort supplierPropertyCommandPort,
                                          SupplierSyncLogCommandPort supplierSyncLogCommandPort,
                                          PropertyFactory propertyFactory) {
        this.propertyCommandPort = propertyCommandPort;
        this.supplierPropertyCommandPort = supplierPropertyCommandPort;
        this.supplierSyncLogCommandPort = supplierSyncLogCommandPort;
        this.propertyFactory = propertyFactory;
    }

    /**
     * Diff 결과를 하나의 트랜잭션으로 저장한다.
     * Diff가 필요한 모든 데이터(supplierId, occurredAt)를 이미 갖고 있다.
     */
    @Transactional
    public void sync(SupplierSyncDiff diff) {
        for (SupplierPropertyData added : diff.added()) {
            Property property = propertyFactory.createFromSupplier(added);
            Long propertyId = propertyCommandPort.persist(property);

            SupplierProperty mapping = SupplierProperty.forNew(
                    diff.supplierId(), PropertyId.of(propertyId),
                    added.externalPropertyId(), diff.occurredAt()
            );
            supplierPropertyCommandPort.persist(mapping);
        }

        for (SupplierProperty deleted : diff.deleted()) {
            deleted.unmap(diff.occurredAt());
            supplierPropertyCommandPort.persist(deleted);
        }

        SupplierSyncLog syncLog = SupplierSyncLog.forSuccess(
                diff.supplierId(), SupplierSyncType.PROPERTY, diff.occurredAt(),
                diff.totalCount(), diff.added().size(),
                diff.updated().size(), diff.deleted().size()
        );
        supplierSyncLogCommandPort.persist(syncLog);
    }
}

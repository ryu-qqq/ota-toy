package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierSyncLogEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierSyncLogJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SupplierSyncLog Query Adapter.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncLogQueryAdapter implements SupplierSyncLogQueryPort {

    private final SupplierSyncLogJpaRepository jpaRepository;
    private final SupplierSyncLogEntityMapper mapper;

    public SupplierSyncLogQueryAdapter(SupplierSyncLogJpaRepository jpaRepository,
                                        SupplierSyncLogEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<SupplierSyncLog> findLastSuccessBySupplierId(SupplierId supplierId, SupplierSyncType syncType) {
        return jpaRepository.findFirstBySupplierIdAndSyncTypeAndStatusOrderBySyncedAtDesc(
                        supplierId.value(), syncType.name(), SupplierSyncStatus.SUCCESS.name())
                .map(mapper::toDomain);
    }
}

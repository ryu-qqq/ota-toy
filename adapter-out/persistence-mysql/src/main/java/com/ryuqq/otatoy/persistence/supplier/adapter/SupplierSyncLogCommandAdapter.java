package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierSyncLogJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierSyncLogEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierSyncLogJpaRepository;
import org.springframework.stereotype.Component;

/**
 * SupplierSyncLog Command Adapter.
 * SupplierSyncLogCommandPort를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncLogCommandAdapter implements SupplierSyncLogCommandPort {

    private final SupplierSyncLogJpaRepository jpaRepository;
    private final SupplierSyncLogEntityMapper mapper;

    public SupplierSyncLogCommandAdapter(SupplierSyncLogJpaRepository jpaRepository,
                                          SupplierSyncLogEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(SupplierSyncLog syncLog) {
        SupplierSyncLogJpaEntity entity = mapper.toEntity(syncLog);
        SupplierSyncLogJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

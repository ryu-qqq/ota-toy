package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierRawDataJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierRawDataEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierRawDataJpaRepository;
import org.springframework.stereotype.Component;

/**
 * SupplierRawData Command Adapter.
 * SupplierRawDataCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataCommandAdapter implements SupplierRawDataCommandPort {

    private final SupplierRawDataJpaRepository jpaRepository;
    private final SupplierRawDataEntityMapper mapper;

    public SupplierRawDataCommandAdapter(SupplierRawDataJpaRepository jpaRepository,
                                          SupplierRawDataEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(SupplierRawData rawData) {
        SupplierRawDataJpaEntity entity = mapper.toEntity(rawData);
        SupplierRawDataJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

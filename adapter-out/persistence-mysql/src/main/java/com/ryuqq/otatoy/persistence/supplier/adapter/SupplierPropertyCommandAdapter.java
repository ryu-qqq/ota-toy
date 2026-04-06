package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierPropertyCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierPropertyJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierPropertyEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierPropertyJpaRepository;
import org.springframework.stereotype.Component;

/**
 * SupplierProperty Command Adapter.
 * SupplierPropertyCommandPort를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierPropertyCommandAdapter implements SupplierPropertyCommandPort {

    private final SupplierPropertyJpaRepository jpaRepository;
    private final SupplierPropertyEntityMapper mapper;

    public SupplierPropertyCommandAdapter(SupplierPropertyJpaRepository jpaRepository,
                                           SupplierPropertyEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(SupplierProperty supplierProperty) {
        SupplierPropertyJpaEntity entity = mapper.toEntity(supplierProperty);
        SupplierPropertyJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

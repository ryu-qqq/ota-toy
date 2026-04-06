package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierPropertyQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierPropertyEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierPropertyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierProperty Query Adapter.
 * SupplierPropertyQueryPort를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierPropertyQueryAdapter implements SupplierPropertyQueryPort {

    private final SupplierPropertyJpaRepository jpaRepository;
    private final SupplierPropertyEntityMapper mapper;

    public SupplierPropertyQueryAdapter(SupplierPropertyJpaRepository jpaRepository,
                                         SupplierPropertyEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SupplierProperty> findBySupplierId(SupplierId supplierId) {
        return jpaRepository.findBySupplierIdAndDeletedFalse(supplierId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

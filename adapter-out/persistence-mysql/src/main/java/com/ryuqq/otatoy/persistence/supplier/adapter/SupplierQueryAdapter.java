package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierQueryPort;
import com.ryuqq.otatoy.domain.supplier.Supplier;
import com.ryuqq.otatoy.domain.supplier.SupplierStatus;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Supplier Query Adapter.
 * SupplierQueryPort를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierQueryAdapter implements SupplierQueryPort {

    private final SupplierJpaRepository jpaRepository;
    private final SupplierEntityMapper mapper;

    public SupplierQueryAdapter(SupplierJpaRepository jpaRepository,
                                 SupplierEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Supplier> findByStatus(SupplierStatus status) {
        return jpaRepository.findByStatusAndDeletedFalse(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

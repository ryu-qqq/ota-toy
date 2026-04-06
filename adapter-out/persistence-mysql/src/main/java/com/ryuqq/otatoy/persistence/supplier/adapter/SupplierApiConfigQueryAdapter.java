package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierApiConfigQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierApiConfigEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierApiConfigJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * SupplierApiConfig Query Adapter.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierApiConfigQueryAdapter implements SupplierApiConfigQueryPort {

    private final SupplierApiConfigJpaRepository jpaRepository;
    private final SupplierApiConfigEntityMapper mapper;

    public SupplierApiConfigQueryAdapter(SupplierApiConfigJpaRepository jpaRepository,
                                          SupplierApiConfigEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SupplierApiConfig> findAllActive() {
        return jpaRepository.findByStatusAndDeletedFalse("ACTIVE")
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SupplierApiConfig> findBySupplierId(SupplierId supplierId) {
        return jpaRepository.findBySupplierIdAndDeletedFalse(supplierId.value())
                .map(mapper::toDomain);
    }
}

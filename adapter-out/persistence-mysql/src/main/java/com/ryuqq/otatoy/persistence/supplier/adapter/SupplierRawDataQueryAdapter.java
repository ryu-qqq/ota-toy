package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierRawDataEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierRawDataJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierRawData Query Adapter.
 * SupplierRawDataQueryPort를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataQueryAdapter implements SupplierRawDataQueryPort {

    private final SupplierRawDataJpaRepository jpaRepository;
    private final SupplierRawDataEntityMapper mapper;

    public SupplierRawDataQueryAdapter(SupplierRawDataJpaRepository jpaRepository,
                                        SupplierRawDataEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SupplierRawData> findBySupplierIdAndStatus(SupplierId supplierId, SupplierRawDataStatus status) {
        return jpaRepository.findBySupplierIdAndStatus(supplierId.value(), status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

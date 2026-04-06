package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierTaskEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierTaskJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierTask Query Adapter.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskQueryAdapter implements SupplierTaskQueryPort {

    private final SupplierTaskJpaRepository jpaRepository;
    private final SupplierTaskEntityMapper mapper;

    public SupplierTaskQueryAdapter(SupplierTaskJpaRepository jpaRepository,
                                     SupplierTaskEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SupplierTask> findByStatus(SupplierTaskStatus status, int limit) {
        return jpaRepository.findByStatus(status.name(), PageRequest.of(0, limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupplierTask> findFailedRetryable(int limit) {
        return jpaRepository.findFailedRetryable(PageRequest.of(0, limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

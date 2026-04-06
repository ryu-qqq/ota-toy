package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierTaskEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierTaskJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierTask Command Adapter.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskCommandAdapter implements SupplierTaskCommandPort {

    private final SupplierTaskJpaRepository jpaRepository;
    private final SupplierTaskEntityMapper mapper;

    public SupplierTaskCommandAdapter(SupplierTaskJpaRepository jpaRepository,
                                       SupplierTaskEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(SupplierTask task) {
        jpaRepository.save(mapper.toEntity(task));
    }

    @Override
    public void persistAll(List<SupplierTask> tasks) {
        jpaRepository.saveAll(tasks.stream().map(mapper::toEntity).toList());
    }
}

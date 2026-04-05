package com.ryuqq.otatoy.persistence.inventory.adapter;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import com.ryuqq.otatoy.persistence.inventory.mapper.InventoryEntityMapper;
import com.ryuqq.otatoy.persistence.inventory.repository.InventoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inventory Command Adapter.
 * InventoryCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class InventoryCommandAdapter implements InventoryCommandPort {

    private final InventoryJpaRepository jpaRepository;
    private final InventoryEntityMapper mapper;

    public InventoryCommandAdapter(InventoryJpaRepository jpaRepository, InventoryEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persistAll(List<Inventory> inventories) {
        List<InventoryJpaEntity> entities = inventories.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

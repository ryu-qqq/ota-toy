package com.ryuqq.otatoy.persistence.inventory.repository;

import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Inventory Command 전용 JPA Repository.
 * save/saveAll만 사용. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, Long> {
}

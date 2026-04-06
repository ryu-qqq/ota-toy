package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Supplier JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, Long> {

    List<SupplierJpaEntity> findByStatusAndDeletedFalse(String status);
}

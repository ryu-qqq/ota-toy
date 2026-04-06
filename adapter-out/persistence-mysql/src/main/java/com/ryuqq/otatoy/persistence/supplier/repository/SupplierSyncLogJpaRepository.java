package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierSyncLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SupplierSyncLog JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierSyncLogJpaRepository extends JpaRepository<SupplierSyncLogJpaEntity, Long> {
}

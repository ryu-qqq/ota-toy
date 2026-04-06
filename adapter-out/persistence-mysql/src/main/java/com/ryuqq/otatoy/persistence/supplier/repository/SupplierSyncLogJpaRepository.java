package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierSyncLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SupplierSyncLog JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierSyncLogJpaRepository extends JpaRepository<SupplierSyncLogJpaEntity, Long> {

    Optional<SupplierSyncLogJpaEntity> findFirstBySupplierIdAndSyncTypeAndStatusOrderBySyncedAtDesc(
            Long supplierId, String syncType, String status);
}

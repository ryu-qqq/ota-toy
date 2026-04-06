package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierApiConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * SupplierApiConfig JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierApiConfigJpaRepository extends JpaRepository<SupplierApiConfigJpaEntity, Long> {

    List<SupplierApiConfigJpaEntity> findByStatusAndDeletedFalse(String status);

    Optional<SupplierApiConfigJpaEntity> findBySupplierIdAndDeletedFalse(Long supplierId);
}

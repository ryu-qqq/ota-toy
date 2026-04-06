package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierRawDataJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SupplierRawData JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRawDataJpaRepository extends JpaRepository<SupplierRawDataJpaEntity, Long> {

    List<SupplierRawDataJpaEntity> findBySupplierIdAndStatus(Long supplierId, String status);

    List<SupplierRawDataJpaEntity> findByStatus(String status);

    List<SupplierRawDataJpaEntity> findByStatus(String status, Pageable pageable);
}

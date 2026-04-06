package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierPropertyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SupplierProperty JPA Repository.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierPropertyJpaRepository extends JpaRepository<SupplierPropertyJpaEntity, Long> {

    List<SupplierPropertyJpaEntity> findBySupplierIdAndDeletedFalse(Long supplierId);
}

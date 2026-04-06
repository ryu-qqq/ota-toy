package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierTaskJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierTaskJpaRepository extends JpaRepository<SupplierTaskJpaEntity, Long> {

    List<SupplierTaskJpaEntity> findByStatus(String status, Pageable pageable);

    @Query("SELECT t FROM SupplierTaskJpaEntity t WHERE t.status = 'FAILED' AND t.retryCount < t.maxRetries ORDER BY t.createdAt ASC")
    List<SupplierTaskJpaEntity> findFailedRetryable(Pageable pageable);
}

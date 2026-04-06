package com.ryuqq.otatoy.persistence.supplier.repository;

import com.ryuqq.otatoy.persistence.supplier.entity.SupplierRoomTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRoomTypeJpaRepository extends JpaRepository<SupplierRoomTypeJpaEntity, Long> {

    List<SupplierRoomTypeJpaEntity> findBySupplierPropertyIdIn(List<Long> supplierPropertyIds);
}

package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRoomTypeQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierRoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierPropertyJpaRepository;
import com.ryuqq.otatoy.persistence.supplier.repository.SupplierRoomTypeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierRoomType Query Adapter.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRoomTypeQueryAdapter implements SupplierRoomTypeQueryPort {

    private final SupplierPropertyJpaRepository propertyJpaRepository;
    private final SupplierRoomTypeJpaRepository roomTypeJpaRepository;
    private final SupplierRoomTypeEntityMapper mapper;

    public SupplierRoomTypeQueryAdapter(SupplierPropertyJpaRepository propertyJpaRepository,
                                         SupplierRoomTypeJpaRepository roomTypeJpaRepository,
                                         SupplierRoomTypeEntityMapper mapper) {
        this.propertyJpaRepository = propertyJpaRepository;
        this.roomTypeJpaRepository = roomTypeJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SupplierRoomType> findBySupplierId(SupplierId supplierId) {
        List<Long> supplierPropertyIds = propertyJpaRepository
                .findBySupplierIdAndDeletedFalse(supplierId.value())
                .stream()
                .map(e -> e.getId())
                .toList();

        if (supplierPropertyIds.isEmpty()) {
            return List.of();
        }

        return roomTypeJpaRepository.findBySupplierPropertyIdIn(supplierPropertyIds)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

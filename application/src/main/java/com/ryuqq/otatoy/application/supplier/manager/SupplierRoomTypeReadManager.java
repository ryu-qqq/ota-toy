package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRoomTypeQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SupplierRoomType 조회 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRoomTypeReadManager {

    private final SupplierRoomTypeQueryPort supplierRoomTypeQueryPort;

    public SupplierRoomTypeReadManager(SupplierRoomTypeQueryPort supplierRoomTypeQueryPort) {
        this.supplierRoomTypeQueryPort = supplierRoomTypeQueryPort;
    }

    @Transactional(readOnly = true)
    public List<SupplierRoomType> findBySupplierId(SupplierId supplierId) {
        return supplierRoomTypeQueryPort.findBySupplierId(supplierId);
    }
}

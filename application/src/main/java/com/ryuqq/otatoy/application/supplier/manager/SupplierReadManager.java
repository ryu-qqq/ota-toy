package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierQueryPort;
import com.ryuqq.otatoy.domain.supplier.Supplier;
import com.ryuqq.otatoy.domain.supplier.SupplierStatus;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Supplier 조회 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierReadManager {

    private final SupplierQueryPort supplierQueryPort;

    public SupplierReadManager(SupplierQueryPort supplierQueryPort) {
        this.supplierQueryPort = supplierQueryPort;
    }

    public List<Supplier> findActiveSuppliers() {
        return supplierQueryPort.findByStatus(SupplierStatus.ACTIVE);
    }
}

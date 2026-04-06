package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierPropertyQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierProperty 조회 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierPropertyReadManager {

    private final SupplierPropertyQueryPort supplierPropertyQueryPort;

    public SupplierPropertyReadManager(SupplierPropertyQueryPort supplierPropertyQueryPort) {
        this.supplierPropertyQueryPort = supplierPropertyQueryPort;
    }

    public List<SupplierProperty> findBySupplierId(SupplierId supplierId) {
        return supplierPropertyQueryPort.findBySupplierId(supplierId);
    }
}

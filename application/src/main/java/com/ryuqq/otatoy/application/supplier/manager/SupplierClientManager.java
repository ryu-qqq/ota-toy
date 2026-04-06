package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierClient;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import org.springframework.stereotype.Component;

/**
 * 외부 공급자 API 호출 관리자.
 * SupplierClient Port를 감싸서 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierClientManager {

    private final SupplierClient supplierClient;

    public SupplierClientManager(SupplierClient supplierClient) {
        this.supplierClient = supplierClient;
    }

    public SupplierFetchResult fetchProperties(SupplierId supplierId) {
        return supplierClient.fetchProperties(supplierId);
    }
}

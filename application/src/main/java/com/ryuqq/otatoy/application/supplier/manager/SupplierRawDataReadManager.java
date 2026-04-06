package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SupplierRawData 조회 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataReadManager {

    private final SupplierRawDataQueryPort rawDataQueryPort;

    public SupplierRawDataReadManager(SupplierRawDataQueryPort rawDataQueryPort) {
        this.rawDataQueryPort = rawDataQueryPort;
    }

    public List<SupplierRawData> findFetched(SupplierId supplierId) {
        return rawDataQueryPort.findBySupplierIdAndStatus(supplierId, SupplierRawDataStatus.FETCHED);
    }
}

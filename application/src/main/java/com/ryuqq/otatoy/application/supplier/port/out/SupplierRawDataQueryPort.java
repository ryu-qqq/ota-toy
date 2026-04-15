package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.util.List;

/**
 * SupplierRawData 조회 전용 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRawDataQueryPort {

    List<SupplierRawData> findBySupplierIdAndStatus(SupplierId supplierId, SupplierRawDataStatus status);

    List<SupplierRawData> findByStatus(SupplierRawDataStatus status);

    List<SupplierRawData> findByStatusWithLimit(SupplierRawDataStatus status, int limit);
}

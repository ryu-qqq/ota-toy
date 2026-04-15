package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

/**
 * SupplierRawData 저장 전용 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRawDataCommandPort {

    Long persist(SupplierRawData rawData);
}

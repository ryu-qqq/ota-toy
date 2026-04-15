package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.util.List;

/**
 * SupplierProperty 조회 전용 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierPropertyQueryPort {

    List<SupplierProperty> findBySupplierId(SupplierId supplierId);
}

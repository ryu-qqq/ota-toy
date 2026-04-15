package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.util.List;
import java.util.Optional;

/**
 * 공급자 API 설정 조회 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierApiConfigQueryPort {

    List<SupplierApiConfig> findAllActive();

    Optional<SupplierApiConfig> findBySupplierId(SupplierId supplierId);
}

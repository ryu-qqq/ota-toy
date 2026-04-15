package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;

import java.util.Optional;

/**
 * SupplierSyncLog 조회 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierSyncLogQueryPort {

    /**
     * 특정 공급자의 특정 동기화 유형에서 마지막 성공 로그를 조회한다.
     */
    Optional<SupplierSyncLog> findLastSuccessBySupplierId(SupplierId supplierId, SupplierSyncType syncType);
}

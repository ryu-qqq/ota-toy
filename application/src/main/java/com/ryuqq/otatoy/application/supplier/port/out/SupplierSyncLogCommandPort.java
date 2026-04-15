package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;

/**
 * SupplierSyncLog 저장 전용 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierSyncLogCommandPort {

    Long persist(SupplierSyncLog syncLog);
}

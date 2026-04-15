package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierTask;

import java.util.List;

/**
 * SupplierTask 저장 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierTaskCommandPort {

    /**
     * SupplierTask 1건을 저장한다.
     */
    void persist(SupplierTask task);

    /**
     * SupplierTask 여러 건을 일괄 저장한다.
     */
    void persistAll(List<SupplierTask> tasks);
}

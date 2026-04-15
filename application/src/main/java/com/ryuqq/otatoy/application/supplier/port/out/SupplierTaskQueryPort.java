package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;

import java.util.List;

/**
 * SupplierTask 조회 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierTaskQueryPort {

    /**
     * 특정 상태의 Task를 배치 크기만큼 조회한다.
     */
    List<SupplierTask> findByStatus(SupplierTaskStatus status, int limit);

    /**
     * 재시도 가능한 FAILED Task를 조회한다.
     */
    List<SupplierTask> findFailedRetryable(int limit);
}

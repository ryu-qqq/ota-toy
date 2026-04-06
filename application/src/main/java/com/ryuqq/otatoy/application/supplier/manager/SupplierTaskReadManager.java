package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SupplierTask 조회 관리자.
 * 읽기 전용 트랜잭션으로 Task를 조회한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskReadManager {

    private final SupplierTaskQueryPort taskQueryPort;

    public SupplierTaskReadManager(SupplierTaskQueryPort taskQueryPort) {
        this.taskQueryPort = taskQueryPort;
    }

    /**
     * PENDING 상태의 Task를 배치 크기만큼 조회한다.
     */
    @Transactional(readOnly = true)
    public List<SupplierTask> findPending(int batchSize) {
        return taskQueryPort.findByStatus(SupplierTaskStatus.PENDING, batchSize);
    }

    /**
     * 재시도 가능한 FAILED Task를 조회한다.
     */
    @Transactional(readOnly = true)
    public List<SupplierTask> findFailedRetryable(int limit) {
        return taskQueryPort.findFailedRetryable(limit);
    }

    /**
     * 진행 중(PENDING, PROCESSING) 상태의 모든 Task를 조회한다.
     * 중복 Task 생성 방지에 사용.
     */
    @Transactional(readOnly = true)
    public SupplierTasks findInProgress() {
        List<SupplierTask> inProgress = new ArrayList<>();
        inProgress.addAll(taskQueryPort.findByStatus(SupplierTaskStatus.PENDING, Integer.MAX_VALUE));
        inProgress.addAll(taskQueryPort.findByStatus(SupplierTaskStatus.PROCESSING, Integer.MAX_VALUE));
        return SupplierTasks.from(inProgress);
    }
}

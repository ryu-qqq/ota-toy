package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SupplierTask 저장/상태변경 트랜잭션 관리자.
 * 메서드 단위 @Transactional로 트랜잭션 경계를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskCommandManager {

    private final SupplierTaskCommandPort taskCommandPort;

    public SupplierTaskCommandManager(SupplierTaskCommandPort taskCommandPort) {
        this.taskCommandPort = taskCommandPort;
    }

    /**
     * Task 1건을 저장한다.
     */
    @Transactional
    public void persist(SupplierTask task) {
        taskCommandPort.persist(task);
    }

    /**
     * Task 여러 건을 일괄 저장한다.
     */
    @Transactional
    public void persistAll(List<SupplierTask> tasks) {
        taskCommandPort.persistAll(tasks);
    }
}

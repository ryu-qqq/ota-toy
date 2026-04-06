package com.ryuqq.otatoy.application.supplier.handler;

import com.ryuqq.otatoy.application.supplier.factory.SupplierTaskFactory;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.springframework.stereotype.Component;

/**
 * PROPERTY_CONTENT Task 완료 시 후속 RATE_AVAILABILITY Task를 등록한다.
 * 숙소 컨텐츠 수집 후 요금·재고 수집으로 이어지는 순차 흐름을 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskFollowUpHandler {

    private final SupplierApiConfigReadManager apiConfigReadManager;
    private final SupplierTaskFactory taskFactory;
    private final SupplierTaskCommandManager taskCommandManager;

    public SupplierTaskFollowUpHandler(SupplierApiConfigReadManager apiConfigReadManager,
                                        SupplierTaskFactory taskFactory,
                                        SupplierTaskCommandManager taskCommandManager) {
        this.apiConfigReadManager = apiConfigReadManager;
        this.taskFactory = taskFactory;
        this.taskCommandManager = taskCommandManager;
    }

    /**
     * 완료된 Task가 PROPERTY_CONTENT인 경우 RATE_AVAILABILITY Task를 생성·저장한다.
     * 그 외 Task 타입에서는 아무 것도 하지 않는다.
     */
    public void handleCompletion(SupplierTask completedTask) {
        if (!completedTask.isTaskType(SupplierTaskType.PROPERTY_CONTENT)) {
            return;
        }

        SupplierApiConfig config = apiConfigReadManager.findBySupplierId(completedTask.supplierId());
        SupplierTask followUpTask = taskFactory.create(config, SupplierTaskType.RATE_AVAILABILITY);
        taskCommandManager.persist(followUpTask);
    }
}

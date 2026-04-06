package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.command.CreateSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.factory.SupplierTaskFactory;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.application.supplier.port.in.CreateSupplierTaskUseCase;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskReadManager;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 수집 주기가 도래한 공급자를 판별하여 SupplierTask를 생성하는 Service.
 * SupplierApiConfig.isDueForFetch()로 수집 대상을 판별하고,
 * PROPERTY_CONTENT, RATE_AVAILABILITY 각각에 대해 Task를 생성한다.
 *
 * Service에 @Transactional 없음 (APP-SVC-001).
 * 트랜잭션은 SupplierTaskCommandManager에서 관리��다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CreateSupplierTaskService implements CreateSupplierTaskUseCase {

    private final SupplierApiConfigReadManager apiConfigReadManager;
    private final SupplierTaskFactory taskFactory;
    private final SupplierTaskReadManager taskReadManager;
    private final SupplierTaskCommandManager taskCommandManager;

    public CreateSupplierTaskService(SupplierApiConfigReadManager apiConfigReadManager,
                                      SupplierTaskFactory taskFactory,
                                      SupplierTaskReadManager taskReadManager,
                                      SupplierTaskCommandManager taskCommandManager) {
        this.apiConfigReadManager = apiConfigReadManager;
        this.taskFactory = taskFactory;
        this.taskReadManager = taskReadManager;
        this.taskCommandManager = taskCommandManager;
    }

    @Override
    public void execute(CreateSupplierTaskCommand command) {
        List<SupplierApiConfig> dueConfigs = apiConfigReadManager.findDueForFetch(command.now());

        if (dueConfigs.isEmpty()) {
            return;
        }

        SupplierTasks candidates = taskFactory.createCandidates(dueConfigs);
        SupplierTasks inProgress = taskReadManager.findInProgress();
        SupplierTasks newTasks = inProgress.excludeDuplicates(candidates);

        if (!newTasks.isEmpty()) {
            taskCommandManager.persistAll(newTasks.items());
        }
    }
}

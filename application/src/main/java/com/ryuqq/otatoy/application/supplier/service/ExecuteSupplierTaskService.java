package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.command.ExecuteSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.facade.SupplierFetchPersistenceFacade;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskReadManager;
import com.ryuqq.otatoy.application.supplier.port.in.ExecuteSupplierTaskUseCase;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategy;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategyProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskFailureReason;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * PENDING 상태의 SupplierTask를 소비하여 외부 API 호출 + RawData 저장을 수행하는 Service.
 *
 * 흐름:
 * 1. PENDING Task 배치 조회
 * 2. 각 Task에 대해:
 *    a. markProcessing() -> 상태 저장
 *    b. SupplierStrategy.fetch() -> 외부 API 호출
 *    c. 성공: completeFetch() -> markCompleted()
 *    d. 실패: markFailed(failureReason) -> 상태 저장
 * 3. PROPERTY_CONTENT 완료 시 후속 RATE_AVAILABILITY Task 자동 생성
 *
 * Service에 @Transactional 없음 (APP-SVC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class ExecuteSupplierTaskService implements ExecuteSupplierTaskUseCase {

    private final SupplierTaskReadManager taskReadManager;
    private final SupplierTaskCommandManager taskCommandManager;
    private final SupplierApiConfigReadManager apiConfigReadManager;
    private final SupplierStrategyProvider strategyProvider;
    private final SupplierFetchPersistenceFacade fetchFacade;

    public ExecuteSupplierTaskService(SupplierTaskReadManager taskReadManager,
                                       SupplierTaskCommandManager taskCommandManager,
                                       SupplierApiConfigReadManager apiConfigReadManager,
                                       SupplierStrategyProvider strategyProvider,
                                       SupplierFetchPersistenceFacade fetchFacade) {
        this.taskReadManager = taskReadManager;
        this.taskCommandManager = taskCommandManager;
        this.apiConfigReadManager = apiConfigReadManager;
        this.strategyProvider = strategyProvider;
        this.fetchFacade = fetchFacade;
    }

    @Override
    public void execute(ExecuteSupplierTaskCommand command) {
        List<SupplierTask> pendingTasks = taskReadManager.findPending(command.batchSize());

        for (SupplierTask task : pendingTasks) {
            processTask(task, command.now());
        }
    }

    private void processTask(SupplierTask task, Instant now) {
        task.markProcessing();
        taskCommandManager.persist(task);

        try {
            SupplierApiConfig config = apiConfigReadManager.findBySupplierId(task.supplierId());
            SupplierStrategy strategy = strategyProvider.getStrategy(config.apiType());
            SupplierFetchResult result = strategy.fetch(config);

            fetchFacade.completeFetch(result, task, config.apiType(), now);

        } catch (com.ryuqq.otatoy.application.common.exception.ExternalServiceUnavailableException e) {
            // CB OPEN — 외부 서비스 자체가 불가. retryCount 안 깎고 PENDING으로 복귀
            task.deferRetry();
            taskCommandManager.persist(task);

        } catch (Exception e) {
            // 일반 실패 — retryCount 증가
            SupplierTaskFailureReason failureReason = SupplierTaskFailureReason.of(
                    null, null, e.getMessage(), now
            );
            task.markFailed(failureReason.toJson(), now);
            taskCommandManager.persist(task);
        }
    }
}

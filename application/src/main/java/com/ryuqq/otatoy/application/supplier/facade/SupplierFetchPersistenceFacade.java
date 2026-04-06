package com.ryuqq.otatoy.application.supplier.facade;

import com.ryuqq.otatoy.application.supplier.handler.SupplierTaskFollowUpHandler;
import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogCommandPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 공급자 데이터 수집 완료 Facade.
 * RawData 저장 + SyncLog 기록 + Task 완료 + 후속 Task 생성을 하나의 트랜잭션으로 묶는다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierFetchPersistenceFacade {

    private final SupplierRawDataCommandPort rawDataCommandPort;
    private final SupplierSyncLogCommandPort syncLogCommandPort;
    private final SupplierTaskCommandPort taskCommandPort;
    private final SupplierTaskFollowUpHandler taskFollowUpHandler;

    public SupplierFetchPersistenceFacade(SupplierRawDataCommandPort rawDataCommandPort,
                                           SupplierSyncLogCommandPort syncLogCommandPort,
                                           SupplierTaskCommandPort taskCommandPort,
                                           SupplierTaskFollowUpHandler taskFollowUpHandler) {
        this.rawDataCommandPort = rawDataCommandPort;
        this.syncLogCommandPort = syncLogCommandPort;
        this.taskCommandPort = taskCommandPort;
        this.taskFollowUpHandler = taskFollowUpHandler;
    }

    /**
     * 수집 성공을 원자적으로 처리한다.
     * 1) RawData 저장 (FETCHED 상태)
     * 2) SyncLog 기록 (FETCH / SUCCESS)
     * 3) Task 완료 처리 (COMPLETED)
     * 4) 후속 Task 생성 (PROPERTY_CONTENT → RATE_AVAILABILITY)
     */
    @Transactional
    public void completeFetch(SupplierFetchResult result, SupplierTask task,
                               SupplierApiType apiType, Instant now) {
        Instant fetchedAt = result.fetchedAt();

        SupplierRawData rawData = SupplierRawData.forNew(
                result.supplierId(), task.taskType(), apiType, result.rawPayload(), fetchedAt);
        rawDataCommandPort.persist(rawData);

        SupplierSyncLog syncLog = SupplierSyncLog.forSuccess(
                result.supplierId(), SupplierSyncType.FETCH, fetchedAt,
                1, 1, 0, 0
        );
        syncLogCommandPort.persist(syncLog);

        task.markCompleted(now);
        taskCommandPort.persist(task);

        taskFollowUpHandler.handleCompletion(task);
    }
}

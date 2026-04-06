package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;

/**
 * SupplierTask 테스트용 Fixture.
 */
class SupplierTaskFixture {

    static final Instant NOW = Instant.parse("2026-04-06T00:00:00Z");
    static final Instant LATER = Instant.parse("2026-04-06T01:00:00Z");
    static final SupplierId DEFAULT_SUPPLIER_ID = SupplierId.of(1L);
    static final Long DEFAULT_API_CONFIG_ID = 10L;
    static final SupplierTaskType DEFAULT_TASK_TYPE = SupplierTaskType.PROPERTY_CONTENT;
    static final String DEFAULT_PAYLOAD = "{\"dateRange\":\"2026-04-01~2026-04-30\"}";
    static final int DEFAULT_MAX_RETRIES = 3;

    static SupplierTask pendingTask() {
        return SupplierTask.forNew(DEFAULT_SUPPLIER_ID, DEFAULT_API_CONFIG_ID,
                DEFAULT_TASK_TYPE, DEFAULT_PAYLOAD, DEFAULT_MAX_RETRIES, NOW);
    }

    static SupplierTask pendingTaskWithMaxRetries(int maxRetries) {
        return SupplierTask.forNew(DEFAULT_SUPPLIER_ID, DEFAULT_API_CONFIG_ID,
                DEFAULT_TASK_TYPE, DEFAULT_PAYLOAD, maxRetries, NOW);
    }

    static SupplierTask processingTask() {
        SupplierTask task = pendingTask();
        task.markProcessing();
        return task;
    }

    static SupplierTask completedTask() {
        SupplierTask task = processingTask();
        task.markCompleted(LATER);
        return task;
    }

    static SupplierTask failedTask() {
        SupplierTask task = processingTask();
        task.markFailed("API 호출 실패", LATER);
        return task;
    }

    static SupplierTask failedTaskWithExhaustedRetries() {
        SupplierTask task = SupplierTask.forNew(DEFAULT_SUPPLIER_ID, DEFAULT_API_CONFIG_ID,
                DEFAULT_TASK_TYPE, DEFAULT_PAYLOAD, 1, NOW);
        task.markProcessing();
        task.markFailed("API 호출 실패", LATER);
        return task;
    }

    static SupplierTask reconstitutedTask(SupplierTaskStatus status, int retryCount) {
        return SupplierTask.reconstitute(
                SupplierTaskId.of(100L), DEFAULT_SUPPLIER_ID, DEFAULT_API_CONFIG_ID,
                DEFAULT_TASK_TYPE, status, DEFAULT_PAYLOAD,
                retryCount, DEFAULT_MAX_RETRIES, null, NOW, null
        );
    }
}

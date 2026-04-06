package com.ryuqq.otatoy.application.supplier.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SupplierTask 도메인 객체 생성 Factory.
 * TimeProvider를 주입받아 시간을 제어한다 (APP-FAC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskFactory {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final TimeProvider timeProvider;

    public SupplierTaskFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * SupplierApiConfig 1건에 대해 지정된 TaskType의 Task를 생성한다.
     */
    public SupplierTask create(SupplierApiConfig config, SupplierTaskType taskType) {
        Instant now = timeProvider.now();
        return SupplierTask.forNew(
                config.supplierId(),
                config.id(),
                taskType,
                null,
                DEFAULT_MAX_RETRIES,
                now
        );
    }

    /**
     * 수집 주기가 도래한 설정 목록에 대해 PROPERTY_CONTENT, RATE_AVAILABILITY 각각의 Task 후보를 생성한다.
     * 반환값은 SupplierTasks 일급 컬렉션이며, 중복 제거는 호출자가 수행한다.
     */
    public SupplierTasks createCandidates(List<SupplierApiConfig> configs) {
        List<SupplierTask> tasks = new ArrayList<>();
        Instant now = timeProvider.now();

        for (SupplierApiConfig config : configs) {
            for (SupplierTaskType taskType : SupplierTaskType.values()) {
                tasks.add(SupplierTask.forNew(
                        config.supplierId(),
                        config.id(),
                        taskType,
                        null,
                        DEFAULT_MAX_RETRIES,
                        now
                ));
            }
        }
        return SupplierTasks.from(tasks);
    }
}

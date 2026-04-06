package com.ryuqq.otatoy.scheduler.supplier;

import com.ryuqq.otatoy.application.supplier.dto.command.CreateSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.port.in.CreateSupplierTaskUseCase;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * [1단계] 공급자 수집 Task 생성 스케줄러.
 * 수집 주기가 도래한 공급자를 판별하여 SupplierTask(PENDING)를 생성한다.
 * 현재 시각을 Command에 담아 UseCase에 전달한다 (ApiMapper 패턴).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskTriggerScheduler {

    private final CreateSupplierTaskUseCase createSupplierTaskUseCase;

    public SupplierTaskTriggerScheduler(CreateSupplierTaskUseCase createSupplierTaskUseCase) {
        this.createSupplierTaskUseCase = createSupplierTaskUseCase;
    }

    @Scheduled(fixedDelay = 60000)
    public void trigger() {
        createSupplierTaskUseCase.execute(CreateSupplierTaskCommand.of(Instant.now()));
    }
}

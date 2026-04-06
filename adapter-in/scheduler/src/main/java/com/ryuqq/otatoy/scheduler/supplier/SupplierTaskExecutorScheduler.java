package com.ryuqq.otatoy.scheduler.supplier;

import com.ryuqq.otatoy.application.supplier.dto.command.ExecuteSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.port.in.ExecuteSupplierTaskUseCase;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * [2단계] 공급자 수집 Task 실행 스케줄러.
 * PENDING 상태 SupplierTask를 소비하여 외부 API 호출 + RawData 저장.
 * 배치 크기와 현재 시각을 Command에 담아 UseCase에 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTaskExecutorScheduler {

    private static final int BATCH_SIZE = 50;

    private final ExecuteSupplierTaskUseCase executeSupplierTaskUseCase;

    public SupplierTaskExecutorScheduler(ExecuteSupplierTaskUseCase executeSupplierTaskUseCase) {
        this.executeSupplierTaskUseCase = executeSupplierTaskUseCase;
    }

    @Scheduled(fixedDelay = 5000)
    public void execute() {
        executeSupplierTaskUseCase.execute(ExecuteSupplierTaskCommand.of(BATCH_SIZE, Instant.now()));
    }
}

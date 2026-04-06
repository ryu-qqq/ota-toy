package com.ryuqq.otatoy.scheduler.supplier;

import com.ryuqq.otatoy.application.supplier.dto.command.ProcessSupplierRawDataCommand;
import com.ryuqq.otatoy.application.supplier.port.in.ProcessSupplierRawDataUseCase;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * [3단계] 공급자 RawData 가공 스케줄러.
 * FETCHED 상태 SupplierRawData를 가공하여 도메인 데이터로 동기화한다.
 * 배치 크기와 현재 시각을 Command에 담아 UseCase에 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataProcessorScheduler {

    private static final int BATCH_SIZE = 50;

    private final ProcessSupplierRawDataUseCase processSupplierRawDataUseCase;

    public SupplierRawDataProcessorScheduler(ProcessSupplierRawDataUseCase processSupplierRawDataUseCase) {
        this.processSupplierRawDataUseCase = processSupplierRawDataUseCase;
    }

    @Scheduled(fixedDelay = 10000)
    public void process() {
        processSupplierRawDataUseCase.execute(ProcessSupplierRawDataCommand.of(BATCH_SIZE, Instant.now()));
    }
}

package com.ryuqq.otatoy.application.supplier.dto.command;

import java.time.Instant;

/**
 * SupplierTask 실행 Command.
 * 스케줄러가 배치 크기와 현재 시각을 담아서 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExecuteSupplierTaskCommand(
        int batchSize,
        Instant now
) {

    public static ExecuteSupplierTaskCommand of(int batchSize, Instant now) {
        return new ExecuteSupplierTaskCommand(batchSize, now);
    }
}

package com.ryuqq.otatoy.application.supplier.dto.command;

import java.time.Instant;

/**
 * SupplierRawData 가공 Command.
 * 스케줄러가 현재 시각을 담아서 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ProcessSupplierRawDataCommand(
        int batchSize,
        Instant now
) {

    public static ProcessSupplierRawDataCommand of(int batchSize, Instant now) {
        return new ProcessSupplierRawDataCommand(batchSize, now);
    }
}

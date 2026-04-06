package com.ryuqq.otatoy.application.supplier.dto.command;

import java.time.Instant;

/**
 * SupplierTask 생성 Command.
 * 스케줄러가 현재 시각을 담아서 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateSupplierTaskCommand(
        Instant now
) {

    public static CreateSupplierTaskCommand of(Instant now) {
        return new CreateSupplierTaskCommand(now);
    }
}

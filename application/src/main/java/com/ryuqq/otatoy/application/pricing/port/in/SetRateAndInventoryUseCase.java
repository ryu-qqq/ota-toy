package com.ryuqq.otatoy.application.pricing.port.in;

import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;

/**
 * 요금/재고 설정 UseCase (Inbound Port).
 * RatePlan에 대한 RateRule, RateOverride, Rate 스냅샷, Inventory를 일괄 설정한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface SetRateAndInventoryUseCase {

    void execute(SetRateAndInventoryCommand command);
}

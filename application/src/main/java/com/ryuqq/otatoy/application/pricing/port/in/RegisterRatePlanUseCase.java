package com.ryuqq.otatoy.application.pricing.port.in;

import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;

/**
 * RatePlan 등록 UseCase (Inbound Port).
 * Controller가 호출하는 진입점이다 (APP-UC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RegisterRatePlanUseCase {

    Long execute(RegisterRatePlanCommand command);
}

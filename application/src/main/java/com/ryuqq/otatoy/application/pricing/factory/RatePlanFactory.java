package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.SourceType;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * RatePlan 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 * Extranet(파트너 직접 등록)이면 sourceType=DIRECT, supplierId=null로 고정한다 (AC-4).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanFactory {

    private final TimeProvider timeProvider;

    public RatePlanFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Extranet용 RatePlan 도메인 객체를 생성한다.
     * sourceType은 DIRECT, supplierId는 null로 고정.
     */
    public RatePlan createForDirect(RegisterRatePlanCommand command) {
        Instant now = timeProvider.now();

        return RatePlan.forNew(
            command.roomTypeId(),
            command.name(),
            SourceType.DIRECT,
            null,
            command.cancellationPolicy(),
            command.paymentPolicy(),
            now
        );
    }
}

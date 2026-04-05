package com.ryuqq.otatoy.application.pricing.dto.command;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

/**
 * RatePlan 등록 요청 Command.
 * 필드 타입은 Domain VO를 사용한다 (APP-DTO-001).
 * sourceType은 Factory에서 DIRECT로 고정하므로 Command에 포함하지 않는다 (AC-4).
 * supplierId도 Extranet 등록이므로 null 고정, Command에 포함하지 않는다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterRatePlanCommand(
    RoomTypeId roomTypeId,
    RatePlanName name,
    CancellationPolicy cancellationPolicy,
    PaymentPolicy paymentPolicy
) {

    public static RegisterRatePlanCommand of(RoomTypeId roomTypeId, RatePlanName name,
                                              CancellationPolicy cancellationPolicy,
                                              PaymentPolicy paymentPolicy) {
        return new RegisterRatePlanCommand(roomTypeId, name, cancellationPolicy, paymentPolicy);
    }
}

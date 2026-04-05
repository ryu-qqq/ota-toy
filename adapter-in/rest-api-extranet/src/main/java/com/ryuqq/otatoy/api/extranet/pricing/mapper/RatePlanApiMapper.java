package com.ryuqq.otatoy.api.extranet.pricing.mapper;

import com.ryuqq.otatoy.api.extranet.pricing.dto.RegisterRatePlanApiRequest;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

/**
 * RatePlan API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(String, Boolean, Integer)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RatePlanApiMapper {

    private RatePlanApiMapper() {}

    /**
     * 요금 정책 등록 API 요청을 Application Command로 변환한다.
     * nullable 필드는 기본값으로 처리한다.
     *
     * @param roomTypeId PathVariable에서 받은 객실 유형 ID
     * @param request    요금 정책 등록 요청 DTO
     * @return 요금 정책 등록 Command
     */
    public static RegisterRatePlanCommand toCommand(Long roomTypeId, RegisterRatePlanApiRequest request) {
        boolean freeCancellation = request.freeCancellation() != null && request.freeCancellation();
        boolean nonRefundable = request.nonRefundable() != null && request.nonRefundable();
        int deadlineDays = request.freeCancellationDeadlineDays() != null
            ? request.freeCancellationDeadlineDays() : 0;

        CancellationPolicy cancellationPolicy = CancellationPolicy.of(
            freeCancellation,
            nonRefundable,
            deadlineDays,
            request.cancellationPolicyText()
        );

        return RegisterRatePlanCommand.of(
            RoomTypeId.of(roomTypeId),
            RatePlanName.of(request.name()),
            cancellationPolicy,
            PaymentPolicy.valueOf(request.paymentPolicy())
        );
    }
}

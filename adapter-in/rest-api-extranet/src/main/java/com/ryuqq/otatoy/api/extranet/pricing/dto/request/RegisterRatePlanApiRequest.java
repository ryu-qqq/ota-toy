package com.ryuqq.otatoy.api.extranet.pricing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 요금 정책(RatePlan) 등록 요청 DTO.
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.pricing.mapper.RatePlanApiMapper}에서
 * Domain VO로 변환한다.
 * <p>
 * 무료 취소/환불 불가 상호 배타 검증은 Domain CancellationPolicy에서 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterRatePlanApiRequest(

    @NotBlank(message = "요금 정책명은 필수입니다")
    @Size(max = 200, message = "요금 정책명은 200자 이하입니다")
    String name,

    Boolean freeCancellation,

    Boolean nonRefundable,

    Integer freeCancellationDeadlineDays,

    @Size(max = 2000, message = "취소 정책 설명은 2000자 이하입니다")
    String cancellationPolicyText,

    @NotNull(message = "결제 정책은 필수입니다")
    String paymentPolicy
) {}

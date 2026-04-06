package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.util.List;

/**
 * 예약 라인 생성 Command.
 * 하나의 객실 유형(요금제)에 대한 예약 단위를 표현한다.
 * 필드는 Domain VO를 사용한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateReservationLineCommand(
    RatePlanId ratePlanId,
    int roomCount,
    Money subtotalAmount,
    List<CreateReservationItemCommand> items
) {}

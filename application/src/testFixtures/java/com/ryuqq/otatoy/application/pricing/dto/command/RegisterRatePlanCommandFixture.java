package com.ryuqq.otatoy.application.pricing.dto.command;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

/**
 * RegisterRatePlanCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RegisterRatePlanCommandFixture {

    private RegisterRatePlanCommandFixture() {}

    /**
     * 기본 RatePlan 등록 커맨드 (무료 취소, 선결제)
     */
    public static RegisterRatePlanCommand aRegisterRatePlanCommand() {
        return RegisterRatePlanCommand.of(
            RoomTypeId.of(1L),
            RatePlanName.of("기본 요금제"),
            CancellationPolicy.of(true, false, 3, "체크인 3일 전까지 무료 취소"),
            PaymentPolicy.PREPAY
        );
    }

    /**
     * 지정 RoomTypeId의 RatePlan 등록 커맨드
     */
    public static RegisterRatePlanCommand aRegisterRatePlanCommandWithRoomTypeId(long roomTypeId) {
        return RegisterRatePlanCommand.of(
            RoomTypeId.of(roomTypeId),
            RatePlanName.of("기본 요금제"),
            CancellationPolicy.of(true, false, 3, "체크인 3일 전까지 무료 취소"),
            PaymentPolicy.PREPAY
        );
    }
}

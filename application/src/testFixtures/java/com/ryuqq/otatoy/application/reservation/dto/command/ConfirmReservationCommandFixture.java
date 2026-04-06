package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;

import java.time.LocalDate;
import java.util.List;

/**
 * ConfirmReservationCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class ConfirmReservationCommandFixture {

    private ConfirmReservationCommandFixture() {}

    public static final Long DEFAULT_SESSION_ID = 1L;
    public static final long DEFAULT_CUSTOMER_ID = 1L;
    public static final GuestInfo DEFAULT_GUEST_INFO = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
    public static final String DEFAULT_BOOKING_SNAPSHOT = "{\"roomType\":\"deluxe\"}";

    /**
     * 기본 예약 확정 커맨드
     */
    public static ConfirmReservationCommand aConfirmReservationCommand() {
        return new ConfirmReservationCommand(
            DEFAULT_SESSION_ID,
            DEFAULT_CUSTOMER_ID,
            DEFAULT_GUEST_INFO,
            DEFAULT_BOOKING_SNAPSHOT,
            List.of(aCreateReservationLineCommand())
        );
    }

    /**
     * 특정 세션ID의 예약 확정 커맨드
     */
    public static ConfirmReservationCommand withSessionId(Long sessionId) {
        return new ConfirmReservationCommand(
            sessionId,
            DEFAULT_CUSTOMER_ID,
            DEFAULT_GUEST_INFO,
            DEFAULT_BOOKING_SNAPSHOT,
            List.of(aCreateReservationLineCommand())
        );
    }

    private static CreateReservationLineCommand aCreateReservationLineCommand() {
        return new CreateReservationLineCommand(
            RatePlanId.of(1L),
            1,
            Money.of(200_000),
            List.of(
                new CreateReservationItemCommand(InventoryId.of(100L), LocalDate.of(2026, 4, 10), Money.of(100_000)),
                new CreateReservationItemCommand(InventoryId.of(101L), LocalDate.of(2026, 4, 11), Money.of(100_000))
            )
        );
    }
}

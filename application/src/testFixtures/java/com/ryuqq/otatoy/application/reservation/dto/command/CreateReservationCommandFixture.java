package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * CreateReservationCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class CreateReservationCommandFixture {

    private CreateReservationCommandFixture() {}

    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final RoomTypeId DEFAULT_ROOM_TYPE_ID = RoomTypeId.of(1L);
    public static final long DEFAULT_CUSTOMER_ID = 1L;
    public static final GuestInfo DEFAULT_GUEST_INFO = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
    public static final LocalDate DEFAULT_CHECK_IN = LocalDate.of(2026, 4, 10);
    public static final LocalDate DEFAULT_CHECK_OUT = LocalDate.of(2026, 4, 12);
    public static final int DEFAULT_GUEST_COUNT = 2;
    public static final Money DEFAULT_TOTAL_AMOUNT = Money.of(200_000);
    public static final String DEFAULT_BOOKING_SNAPSHOT = "{\"roomType\":\"deluxe\"}";

    /**
     * 기본 예약 생성 커맨드 (2박, 1라인, 2아이템)
     */
    public static CreateReservationCommand aCreateReservationCommand() {
        return new CreateReservationCommand(
            DEFAULT_PROPERTY_ID,
            DEFAULT_ROOM_TYPE_ID,
            DEFAULT_CUSTOMER_ID,
            DEFAULT_GUEST_INFO,
            DEFAULT_CHECK_IN,
            DEFAULT_CHECK_OUT,
            DEFAULT_GUEST_COUNT,
            DEFAULT_TOTAL_AMOUNT,
            DEFAULT_BOOKING_SNAPSHOT,
            List.of(aCreateReservationLineCommand())
        );
    }

    /**
     * 기본 예약 라인 커맨드
     */
    public static CreateReservationLineCommand aCreateReservationLineCommand() {
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

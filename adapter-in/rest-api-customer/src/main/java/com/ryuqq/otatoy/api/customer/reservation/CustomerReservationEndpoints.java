package com.ryuqq.otatoy.api.customer.reservation;

/**
 * Customer Reservation API 엔드포인트 상수.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class CustomerReservationEndpoints {

    private CustomerReservationEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String BASE = "/api/v1";

    // 예약 세션
    public static final String RESERVATION_SESSIONS = BASE + "/reservation-sessions";

    // 예약
    public static final String RESERVATIONS = BASE + "/reservations";
    public static final String RESERVATION_ID = "/{reservationId}";
    public static final String RESERVATION_BY_ID = RESERVATIONS + RESERVATION_ID;
    public static final String CANCEL = RESERVATION_BY_ID + "/cancel";

    // Path Variable
    public static final String PATH_RESERVATION_ID = "reservationId";
}

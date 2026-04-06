package com.ryuqq.otatoy.api.customer.fixture;

import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.domain.common.vo.Money;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Customer 예약 API 테스트용 Fixture.
 * 요청 JSON과 Mock 응답 데이터를 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class CustomerReservationFixture {

    private CustomerReservationFixture() {}

    public static final String IDEMPOTENCY_KEY = "idem-key-12345";
    public static final Long RESERVATION_ID = 100L;

    // === 요청 JSON ===

    /**
     * 예약 세션 생성 요청 JSON
     */
    public static String createSessionRequest() {
        return """
            {
                "propertyId": 1,
                "roomTypeId": 1,
                "ratePlanId": 1,
                "checkIn": "2026-06-01",
                "checkOut": "2026-06-03",
                "guestCount": 2,
                "totalAmount": 240000
            }
            """;
    }

    /**
     * 예약 세션 생성 시 필수 필드 누락 JSON (propertyId 누락)
     */
    public static String createSessionInvalidRequest() {
        return """
            {
                "roomTypeId": 1,
                "ratePlanId": 1,
                "checkIn": "2026-06-01",
                "checkOut": "2026-06-03",
                "guestCount": 2,
                "totalAmount": 240000
            }
            """;
    }

    /**
     * 예약 확정 요청 JSON
     */
    public static String confirmReservationRequest() {
        return """
            {
                "sessionId": 1,
                "customerId": 100,
                "guestInfo": {
                    "name": "홍길동",
                    "phone": "010-1234-5678",
                    "email": "hong@test.com"
                },
                "bookingSnapshot": "스냅샷 데이터",
                "lines": [
                    {
                        "ratePlanId": 1,
                        "roomCount": 1,
                        "subtotalAmount": 240000,
                        "items": [
                            {
                                "inventoryId": 1,
                                "stayDate": "2026-06-01",
                                "nightlyRate": 120000
                            },
                            {
                                "inventoryId": 2,
                                "stayDate": "2026-06-02",
                                "nightlyRate": 120000
                            }
                        ]
                    }
                ]
            }
            """;
    }

    /**
     * 예약 확정 시 필수 필드 누락 JSON (sessionId 누락)
     */
    public static String confirmReservationInvalidRequest() {
        return """
            {
                "customerId": 100,
                "guestInfo": {
                    "name": "홍길동"
                },
                "lines": [
                    {
                        "ratePlanId": 1,
                        "roomCount": 1,
                        "subtotalAmount": 240000,
                        "items": [
                            {
                                "inventoryId": 1,
                                "stayDate": "2026-06-01",
                                "nightlyRate": 120000
                            }
                        ]
                    }
                ]
            }
            """;
    }

    /**
     * 예약 취소 요청 JSON (사유 포함)
     */
    public static String cancelReservationRequest() {
        return """
            {
                "cancelReason": "일정 변경으로 취소합니다"
            }
            """;
    }

    // === Mock 응답 데이터 ===

    /**
     * 예약 세션 생성 결과
     */
    public static ReservationSessionResult sessionResult() {
        return new ReservationSessionResult(
            1L,
            Money.of(240000),
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 3),
            2,
            Instant.parse("2026-06-01T01:00:00Z")
        );
    }
}

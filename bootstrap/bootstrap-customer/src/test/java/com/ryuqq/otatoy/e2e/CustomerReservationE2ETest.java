package com.ryuqq.otatoy.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Customer 예약 E2E 테스트.
 * 예약 세션 생성 -> 예약 확정 -> 예약 취소 전체 흐름,
 * 멱등키 중복, 이미 취소된 예약 재취소를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("e2e")
@DisplayName("Customer 예약 E2E 테스트")
class CustomerReservationE2ETest extends CustomerE2ETestBase {

    private static final String SESSION_URL = "/api/v1/reservation-sessions";
    private static final String RESERVATION_URL = "/api/v1/reservations";

    @Nested
    @DisplayName("P0: 예약 전체 흐름 (세션 생성 -> 확정 -> 취소)")
    class FullReservationFlow {

        @Test
        @DisplayName("예약 세션 생성 -> 확정 -> 취소 성공")
        void shouldCompleteFullReservationFlow() {
            // given
            ReservationTestData data = setupReservationData(5);
            String idempotencyKey = UUID.randomUUID().toString();

            // 1단계: 예약 세션 생성
            Map<String, Object> sessionRequest = Map.of(
                "propertyId", data.propertyId(),
                "roomTypeId", data.roomTypeId(),
                "ratePlanId", data.ratePlanId(),
                "checkIn", data.checkIn().toString(),
                "checkOut", data.checkOut().toString(),
                "guestCount", 2,
                "totalAmount", data.price()
            );

            HttpHeaders sessionHeaders = new HttpHeaders();
            sessionHeaders.set("Idempotency-Key", idempotencyKey);

            ResponseEntity<Map<String, Object>> sessionResponse = postJsonWithHeaders(
                SESSION_URL, sessionRequest, sessionHeaders);

            assertThat(sessionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Map<String, Object> sessionBody = sessionResponse.getBody();
            assertThat(sessionBody).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = (Map<String, Object>) sessionBody.get("data");
            Long sessionId = ((Number) sessionData.get("sessionId")).longValue();
            assertThat(sessionId).isPositive();
            assertThat(sessionData.get("expiresAt")).isNotNull();

            // 2단계: 예약 확정
            Map<String, Object> confirmRequest = Map.of(
                "sessionId", sessionId,
                "customerId", 1001L,
                "guestInfo", Map.of(
                    "name", "홍길동",
                    "phone", "010-1234-5678",
                    "email", "hong@test.com"
                ),
                "bookingSnapshot", "{}",
                "lines", List.of(
                    Map.of(
                        "ratePlanId", data.ratePlanId(),
                        "roomCount", 1,
                        "subtotalAmount", data.price(),
                        "items", List.of(
                            Map.of(
                                "inventoryId", data.inventoryId(),
                                "stayDate", data.checkIn().toString(),
                                "nightlyRate", data.price()
                            )
                        )
                    )
                )
            );

            ResponseEntity<Map<String, Object>> confirmResponse = postJson(
                RESERVATION_URL, confirmRequest);

            assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long reservationId = ((Number) confirmResponse.getBody().get("data")).longValue();
            assertThat(reservationId).isPositive();

            // DB 검증: totalAmount 일치 여부
            transactionTemplate.executeWithoutResult(status -> {
                var reservation = reservationJpaRepository.findById(reservationId).orElseThrow();
                assertThat(reservation.getTotalAmount()).isEqualByComparingTo(data.price());
            });

            // 재고 검증: 예약 확정 후 availableCount가 1 감소했는지 확인
            transactionTemplate.executeWithoutResult(status -> {
                var inventory = inventoryJpaRepository.findById(data.inventoryId()).orElseThrow();
                assertThat(inventory.getAvailableCount()).isEqualTo(4); // 5 - 1 = 4
            });

            // 3단계: 예약 취소
            Map<String, Object> cancelRequest = Map.of(
                "cancelReason", "고객 변심"
            );

            ResponseEntity<Map<String, Object>> cancelResponse = patchJson(
                RESERVATION_URL + "/" + reservationId + "/cancel", cancelRequest);

            assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // DB 검증: reservation.status = CANCELLED
            transactionTemplate.executeWithoutResult(status -> {
                var reservation = reservationJpaRepository.findById(reservationId).orElseThrow();
                assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
            });

            // 재고 복구 검증: 취소 후 availableCount가 원래 값으로 복구됐는지 확인
            transactionTemplate.executeWithoutResult(status -> {
                var inventory = inventoryJpaRepository.findById(data.inventoryId()).orElseThrow();
                assertThat(inventory.getAvailableCount()).isEqualTo(5); // 원래 값 복구
            });
        }
    }

    @Nested
    @DisplayName("P1: 멱등키 중복 시 동일 세션 반환")
    class IdempotencyKeyDuplication {

        @Test
        @DisplayName("동일 멱등키로 두 번 요청 시 같은 sessionId 반환")
        void shouldReturnSameSessionForDuplicateIdempotencyKey() {
            // given
            ReservationTestData data = setupReservationData(5);
            String idempotencyKey = "dup-key-" + UUID.randomUUID();

            Map<String, Object> sessionRequest = Map.of(
                "propertyId", data.propertyId(),
                "roomTypeId", data.roomTypeId(),
                "ratePlanId", data.ratePlanId(),
                "checkIn", data.checkIn().toString(),
                "checkOut", data.checkOut().toString(),
                "guestCount", 2,
                "totalAmount", data.price()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Idempotency-Key", idempotencyKey);

            // 1차 호출
            ResponseEntity<Map<String, Object>> response1 = postJsonWithHeaders(
                SESSION_URL, sessionRequest, headers);
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            @SuppressWarnings("unchecked")
            Map<String, Object> data1 = (Map<String, Object>) response1.getBody().get("data");
            Long sessionId1 = ((Number) data1.get("sessionId")).longValue();

            // 2차 호출 (동일 키)
            ResponseEntity<Map<String, Object>> response2 = postJsonWithHeaders(
                SESSION_URL, sessionRequest, headers);

            // 같은 세션 반환 (201 또는 200)
            assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();

            @SuppressWarnings("unchecked")
            Map<String, Object> data2 = (Map<String, Object>) response2.getBody().get("data");
            Long sessionId2 = ((Number) data2.get("sessionId")).longValue();

            assertThat(sessionId1).isEqualTo(sessionId2);
        }
    }

    @Nested
    @DisplayName("P1: 이미 취소된 예약 재취소")
    class CancelAlreadyCancelledReservation {

        @Test
        @DisplayName("이미 취소된 예약을 재취소하면 409 Conflict 응답")
        void shouldReturn409ForAlreadyCancelledReservation() {
            // given: 예약 생성 + 취소
            ReservationTestData data = setupReservationData(5);
            String idempotencyKey = UUID.randomUUID().toString();

            // 세션 생성
            Map<String, Object> sessionRequest = Map.of(
                "propertyId", data.propertyId(),
                "roomTypeId", data.roomTypeId(),
                "ratePlanId", data.ratePlanId(),
                "checkIn", data.checkIn().toString(),
                "checkOut", data.checkOut().toString(),
                "guestCount", 2,
                "totalAmount", data.price()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Idempotency-Key", idempotencyKey);

            ResponseEntity<Map<String, Object>> sessionResponse = postJsonWithHeaders(
                SESSION_URL, sessionRequest, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = (Map<String, Object>) sessionResponse.getBody().get("data");
            Long sessionId = ((Number) sessionData.get("sessionId")).longValue();

            // 예약 확정
            Map<String, Object> confirmRequest = Map.of(
                "sessionId", sessionId,
                "customerId", 1001L,
                "guestInfo", Map.of(
                    "name", "홍길동",
                    "phone", "010-1234-5678",
                    "email", "hong@test.com"
                ),
                "bookingSnapshot", "{}",
                "lines", List.of(
                    Map.of(
                        "ratePlanId", data.ratePlanId(),
                        "roomCount", 1,
                        "subtotalAmount", data.price(),
                        "items", List.of(
                            Map.of(
                                "inventoryId", data.inventoryId(),
                                "stayDate", data.checkIn().toString(),
                                "nightlyRate", data.price()
                            )
                        )
                    )
                )
            );

            ResponseEntity<Map<String, Object>> confirmResponse = postJson(
                RESERVATION_URL, confirmRequest);
            Long reservationId = ((Number) confirmResponse.getBody().get("data")).longValue();

            // 1차 취소 (성공)
            patchJson(RESERVATION_URL + "/" + reservationId + "/cancel",
                Map.of("cancelReason", "고객 변심"));

            // 2차 취소 (실패 기대)
            ResponseEntity<Map<String, Object>> reCancelResponse = patchJson(
                RESERVATION_URL + "/" + reservationId + "/cancel",
                Map.of("cancelReason", "재취소 시도"));

            assertThat(reCancelResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }
}

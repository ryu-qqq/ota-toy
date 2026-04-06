package com.ryuqq.otatoy.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Customer 동시성 E2E 테스트.
 * 동일 재고에 대해 다수의 동시 예약 요청이 들어올 때,
 * 재고 정합성이 보장되는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("e2e")
@DisplayName("Customer 동시성 E2E 테스트")
class CustomerConcurrencyE2ETest extends CustomerE2ETestBase {

    private static final String SESSION_URL = "/api/v1/reservation-sessions";

    @Test
    @DisplayName("P0: 재고 1개에 동시 10건 예약 세션 생성 -> 정확히 1건 성공, 9건 실패")
    void shouldAllowOnlyOneReservationWhenInventoryIsOne() throws Exception {
        // given: 재고 1개로 사전 데이터 삽입
        ReservationTestData data = setupReservationData(1);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<ResponseEntity<Map<String, Object>>>> futures = new ArrayList<>();

        // when: 10개 스레드가 동시에 예약 세션 생성 요청
        for (int i = 0; i < threadCount; i++) {
            final String idempotencyKey = "concurrency-" + UUID.randomUUID();

            Future<ResponseEntity<Map<String, Object>>> future = executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await(); // 모든 스레드가 준비될 때까지 대기

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

                return postJsonWithHeaders(SESSION_URL, sessionRequest, headers);
            });

            futures.add(future);
        }

        // 모든 스레드가 준비되면 동시에 시작
        readyLatch.await();
        startLatch.countDown();

        // then: 결과 수집
        List<HttpStatus> statuses = new ArrayList<>();
        for (Future<ResponseEntity<Map<String, Object>>> future : futures) {
            ResponseEntity<Map<String, Object>> response = future.get();
            statuses.add((HttpStatus) response.getStatusCode());
        }

        executor.shutdown();

        // 검증: 정확히 1건 성공 (201), 나머지 실패 (409)
        long successCount = statuses.stream()
            .filter(s -> s == HttpStatus.CREATED)
            .count();
        long failCount = statuses.stream()
            .filter(s -> s == HttpStatus.CONFLICT)
            .count();

        assertThat(successCount).as("재고 1개이므로 정확히 1건만 성공해야 한다").isEqualTo(1);
        assertThat(failCount).as("나머지 9건은 재고 소진으로 실패해야 한다").isEqualTo(9);

        // 세션 생성은 Redis에서만 재고를 차감한다 (DB는 확정 단계에서 차감).
        // 따라서 성공/실패 건수로 Redis 원자적 차감의 정확성을 검증한다.
        assertThat(successCount + failCount)
            .as("전체 요청 수는 스레드 수와 일치해야 한다")
            .isEqualTo(threadCount);
    }
}

package com.ryuqq.otatoy.performance;

import com.ryuqq.otatoy.e2e.CustomerE2ETestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 재고 동시성 성능 테스트.
 * 대규모 동시 예약 요청에서 Redis 원자적 카운터가 정확하게 재고를 차감하는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("performance")
@DisplayName("재고 동시성 성능 테스트")
class InventoryConcurrencyPerformanceTest extends CustomerE2ETestBase {

    private static final String SESSION_URL = "/api/v1/reservation-sessions";

    @Test
    @DisplayName("시나리오 1: 재고 100개에 동시 200건 예약 세션 생성 — 성공 100건, 실패 100건")
    void shouldAllowExactly100ReservationsFromInventory100() throws Exception {
        // given: 재고 100개
        ReservationTestData data = setupReservationData(100);

        int threadCount = 200;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<HttpStatus> statuses = Collections.synchronizedList(new ArrayList<>());
        List<Long> durations = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger progress = new AtomicInteger(0);

        long totalStart = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();

                    long t0 = System.nanoTime();

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
                    headers.set("Idempotency-Key", UUID.randomUUID().toString());

                    ResponseEntity<Map<String, Object>> response =
                        postJsonWithHeaders(SESSION_URL, sessionRequest, headers);

                    durations.add((System.nanoTime() - t0) / 1_000_000);
                    statuses.add((HttpStatus) response.getStatusCode());
                } catch (Exception e) {
                    System.err.println("[재고 200건] 예외 발생: " + e.getMessage());
                } finally {
                    int completed = progress.incrementAndGet();
                    if (completed % 50 == 0) {
                        System.out.println("[재고 100/200] 진행: " + completed + "/" + threadCount);
                    }
                    done.countDown();
                }
            });
        }

        ready.await();
        System.out.println("[재고 100/200] 모든 스레드 준비 완료 → 동시 출발");
        start.countDown();
        boolean finished = done.await(120, TimeUnit.SECONDS);

        long totalMs = (System.nanoTime() - totalStart) / 1_000_000;

        executor.shutdown();

        // then
        assertThat(finished).as("120초 내 전부 완료되어야 한다").isTrue();
        assertThat(statuses).hasSize(threadCount);

        long successCount = statuses.stream()
            .filter(s -> s == HttpStatus.CREATED)
            .count();
        long failCount = statuses.stream()
            .filter(s -> s == HttpStatus.CONFLICT)
            .count();

        System.out.println("[재고 100 / 요청 200] 성공: " + successCount + "건, 실패: " + failCount + "건");
        System.out.println("[재고 100 / 요청 200] 전체 처리 시간: " + totalMs + "ms");

        if (!durations.isEmpty()) {
            long avg = durations.stream().mapToLong(Long::longValue).sum() / durations.size();
            List<Long> sorted = durations.stream().sorted().toList();
            long p99 = sorted.get((int) (sorted.size() * 0.99));
            System.out.println("[재고 100 / 요청 200] 평균: " + avg + "ms, P99: " + p99 + "ms");
        }

        assertThat(successCount).as("재고 100개이므로 정확히 100건 성공해야 한다").isEqualTo(100);
        assertThat(failCount).as("나머지 100건은 재고 소진으로 실패해야 한다").isEqualTo(100);
        assertThat(successCount + failCount).as("전체 요청 수는 200건이어야 한다").isEqualTo(threadCount);
    }

    @Test
    @DisplayName("시나리오 2: 재고 10개에 동시 500건 예약 세션 생성 — 성공 10건, 실패 490건")
    void shouldAllowExactly10ReservationsFromInventory10() throws Exception {
        // given: 재고 10개
        ReservationTestData data = setupReservationData(10);

        int threadCount = 500;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<HttpStatus> statuses = Collections.synchronizedList(new ArrayList<>());
        List<Long> durations = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger progress2 = new AtomicInteger(0);

        long totalStart = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();

                    long t0 = System.nanoTime();

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
                    headers.set("Idempotency-Key", UUID.randomUUID().toString());

                    ResponseEntity<Map<String, Object>> response =
                        postJsonWithHeaders(SESSION_URL, sessionRequest, headers);

                    durations.add((System.nanoTime() - t0) / 1_000_000);
                    statuses.add((HttpStatus) response.getStatusCode());
                } catch (Exception e) {
                    System.err.println("[재고 500건] 예외 발생: " + e.getMessage());
                } finally {
                    int completed = progress2.incrementAndGet();
                    if (completed % 100 == 0) {
                        System.out.println("[재고 10/500] 진행: " + completed + "/" + threadCount);
                    }
                    done.countDown();
                }
            });
        }

        ready.await();
        System.out.println("[재고 10/500] 모든 스레드 준비 완료 → 동시 출발");
        start.countDown();
        boolean finished = done.await(120, TimeUnit.SECONDS);

        long totalMs = (System.nanoTime() - totalStart) / 1_000_000;

        executor.shutdown();

        // then
        assertThat(finished).as("120초 내 전부 완료되어야 한다").isTrue();
        assertThat(statuses).hasSize(threadCount);

        long successCount = statuses.stream()
            .filter(s -> s == HttpStatus.CREATED)
            .count();
        long failCount = statuses.stream()
            .filter(s -> s == HttpStatus.CONFLICT)
            .count();

        System.out.println("[재고 10 / 요청 500] 성공: " + successCount + "건, 실패: " + failCount + "건");
        System.out.println("[재고 10 / 요청 500] 전체 처리 시간: " + totalMs + "ms");

        if (!durations.isEmpty()) {
            long avg = durations.stream().mapToLong(Long::longValue).sum() / durations.size();
            List<Long> sorted = durations.stream().sorted().toList();
            long p99 = sorted.get((int) (sorted.size() * 0.99));
            System.out.println("[재고 10 / 요청 500] 평균: " + avg + "ms, P99: " + p99 + "ms");
        }

        assertThat(successCount).as("재고 10개이므로 정확히 10건 성공해야 한다").isEqualTo(10);
        assertThat(failCount).as("나머지 490건은 재고 소진으로 실패해야 한다").isEqualTo(490);
        assertThat(successCount + failCount).as("전체 요청 수는 500건이어야 한다").isEqualTo(threadCount);
    }
}

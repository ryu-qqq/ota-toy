package com.ryuqq.otatoy.performance;

import com.ryuqq.otatoy.e2e.CustomerE2ETestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 요금 캐싱 성능 테스트.
 * 캐시 콜드 스타트 vs 웜 캐시, 동시 요금 조회, 캐시 무효화 후 재조회 시나리오를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("performance")
@DisplayName("요금 캐싱 성능 테스트")
class RateCachePerformanceTest extends CustomerE2ETestBase {

    private static final String RATE_URL = "/api/v1/properties";

    @Test
    @DisplayName("시나리오 1: 캐시 콜드 스타트 vs 웜 캐시 — 2차 요청이 1차보다 빨라야 한다")
    void shouldBeFasterOnCacheHit() {
        // given: 숙소 1개, 객실 3개, 각 요금정책 + Rate 7일치 + Inventory
        Long partnerId = insertPartner("캐시 성능 파트너");
        Long propertyTypeId = insertPropertyType("HOTEL", "호텔");
        Long propertyId = insertProperty(partnerId, propertyTypeId, "캐시 성능 호텔");

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(7);
        BigDecimal price = new BigDecimal("100000.00");

        for (int r = 0; r < 3; r++) {
            Long roomTypeId = insertRoomType(propertyId, "객실타입_" + r);
            Long ratePlanId = insertRatePlan(roomTypeId, "요금제_" + r);

            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                insertRateRule(ratePlanId, date, date.plusDays(1), price);
                insertRate(ratePlanId, date, price);
                insertInventory(roomTypeId, date, 10, 10);
            }
        }

        String url = String.format(
            "%s%s/%d/rates?checkIn=%s&checkOut=%s&guests=2",
            baseUrl(), RATE_URL, propertyId, checkIn, checkOut);

        // 1차 요청 (캐시 미스)
        long start1 = System.nanoTime();
        ResponseEntity<Map<String, Object>> response1 = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        long coldMs = (System.nanoTime() - start1) / 1_000_000;

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2차 요청 (캐시 히트)
        long start2 = System.nanoTime();
        ResponseEntity<Map<String, Object>> response2 = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        long warmMs = (System.nanoTime() - start2) / 1_000_000;

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 검증: 2차가 1차보다 빨라야 함
        System.out.println("[캐시 콜드/웜] 1차(캐시 미스): " + coldMs + "ms, 2차(캐시 히트): " + warmMs + "ms");
        assertThat(warmMs).as("캐시 히트 응답이 캐시 미스보다 빨라야 한다").isLessThanOrEqualTo(coldMs);
    }

    @Test
    @DisplayName("시나리오 2: 동시 100건 요금 조회 — 전부 200 OK, 평균/P99 응답 시간 기록")
    void shouldHandleConcurrentRateQueries() throws Exception {
        // given
        Long partnerId = insertPartner("동시 요금 파트너");
        Long propertyTypeId = insertPropertyType("HOTEL_CC", "호텔");
        Long propertyId = insertProperty(partnerId, propertyTypeId, "동시 요금 호텔");
        Long roomTypeId = insertRoomType(propertyId, "디럭스");
        Long ratePlanId = insertRatePlan(roomTypeId, "기본 요금제");

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(3);
        BigDecimal price = new BigDecimal("150000.00");

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            insertRateRule(ratePlanId, date, date.plusDays(1), price);
            insertRate(ratePlanId, date, price);
            insertInventory(roomTypeId, date, 10, 10);
        }

        // 캐시 워밍 (1차 조회)
        String url = String.format(
            "%s%s/%d/rates?checkIn=%s&checkOut=%s&guests=2",
            baseUrl(), RATE_URL, propertyId, checkIn, checkOut);

        restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});

        // when: 동시 100건 요청
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Long> durations = Collections.synchronizedList(new ArrayList<>());
        List<HttpStatus> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    long t0 = System.nanoTime();
                    ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                        url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
                    durations.add((System.nanoTime() - t0) / 1_000_000);
                    statuses.add((HttpStatus) resp.getStatusCode());
                } catch (Exception e) {
                    System.err.println("[동시 요금 조회] 예외 발생: " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);

        executor.shutdown();

        // then
        assertThat(finished).as("60초 내 전부 완료되어야 한다").isTrue();
        assertThat(statuses).hasSize(threadCount);
        assertThat(statuses).allMatch(s -> s == HttpStatus.OK);

        long avg = durations.stream().mapToLong(Long::longValue).sum() / durations.size();
        List<Long> sorted = durations.stream().sorted().toList();
        long p99 = sorted.get((int) (sorted.size() * 0.99));

        System.out.println("[동시 100건 요금 조회] 평균: " + avg + "ms, P99: " + p99 + "ms");
    }

    @Test
    @DisplayName("시나리오 3: 캐시 무효화 후 동시 50건 재조회 — 스탬피드 없이 정상 응답")
    void shouldHandleCacheInvalidationWithConcurrentRequests() throws Exception {
        // given
        Long partnerId = insertPartner("캐시 무효화 파트너");
        Long propertyTypeId = insertPropertyType("HOTEL_INV", "호텔");
        Long propertyId = insertProperty(partnerId, propertyTypeId, "캐시 무효화 호텔");
        Long roomTypeId = insertRoomType(propertyId, "스위트");
        Long ratePlanId = insertRatePlan(roomTypeId, "프리미엄 요금제");

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(3);
        BigDecimal price = new BigDecimal("200000.00");

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            insertRateRule(ratePlanId, date, date.plusDays(1), price);
            insertRate(ratePlanId, date, price);
            insertInventory(roomTypeId, date, 10, 10);
        }

        String url = String.format(
            "%s%s/%d/rates?checkIn=%s&checkOut=%s&guests=2",
            baseUrl(), RATE_URL, propertyId, checkIn, checkOut);

        // 1차 조회 (캐시 워밍)
        ResponseEntity<Map<String, Object>> warmResp = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertThat(warmResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Redis FLUSHDB로 캐시 강제 만료
        flushRedis();

        // when: 동시 50건 재조회 (캐시 미스 → DB 접근)
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Long> durations = Collections.synchronizedList(new ArrayList<>());
        List<HttpStatus> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    long t0 = System.nanoTime();
                    ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                        url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
                    durations.add((System.nanoTime() - t0) / 1_000_000);
                    statuses.add((HttpStatus) resp.getStatusCode());
                } catch (Exception e) {
                    System.err.println("[캐시 무효화 재조회] 예외 발생: " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);

        executor.shutdown();

        // then
        assertThat(finished).as("60초 내 전부 완료되어야 한다").isTrue();
        assertThat(statuses).hasSize(threadCount);
        assertThat(statuses).allMatch(s -> s == HttpStatus.OK);

        long avg = durations.stream().mapToLong(Long::longValue).sum() / durations.size();
        List<Long> sorted = durations.stream().sorted().toList();
        long p99 = sorted.get((int) (sorted.size() * 0.99));

        System.out.println("[캐시 무효화 후 50건 재조회] 평균: " + avg + "ms, P99: " + p99 + "ms");
    }

    /**
     * Testcontainers Redis에 FLUSHDB 명령을 실행하여 캐시를 초기화한다.
     */
    private void flushRedis() {
        try {
            redis.execInContainer("redis-cli", "FLUSHDB");
        } catch (Exception e) {
            throw new RuntimeException("Redis FLUSHDB 실행 실패", e);
        }
    }
}

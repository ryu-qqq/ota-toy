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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캐시 무효화 + 가격 변경 시나리오 테스트.
 * 요금 조회 중 DB 가격 변경 → 캐시 무효화 → 새 가격 반영 흐름을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("performance")
@DisplayName("캐시 무효화 + 가격 변경 시나리오 테스트")
class RateCacheInvalidationTest extends CustomerE2ETestBase {

    private static final String RATE_URL = "/api/v1/properties";

    @Test
    @DisplayName("가격 변경 후 캐시 무효화 → 새 가격이 정상 반영된다")
    void shouldReflectNewPriceAfterCacheInvalidation() {
        // given: 숙소 + 객실 + Rate(가격 100,000)
        Long partnerId = insertPartner("캐시 무효화 테스트 파트너");
        Long propertyTypeId = insertPropertyType("HOTEL_CI", "호텔");
        Long propertyId = insertProperty(partnerId, propertyTypeId, "캐시 무효화 테스트 호텔");
        Long roomTypeId = insertRoomType(propertyId, "스탠다드");
        Long ratePlanId = insertRatePlan(roomTypeId, "기본 요금제");

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(1);
        BigDecimal originalPrice = new BigDecimal("100000.00");
        BigDecimal newPrice = new BigDecimal("150000.00");

        insertRateRule(ratePlanId, checkIn, checkOut, originalPrice);
        Long rateId = insertRate(ratePlanId, checkIn, originalPrice);
        insertInventory(roomTypeId, checkIn, 10, 10);

        String url = String.format(
            "%s%s/%d/rates?checkIn=%s&checkOut=%s&guests=2",
            baseUrl(), RATE_URL, propertyId, checkIn, checkOut);

        // 1단계: 1차 조회 → 가격 100,000 확인 (캐시에 적재)
        long start1 = System.nanoTime();
        ResponseEntity<Map<String, Object>> response1 = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        long firstMs = (System.nanoTime() - start1) / 1_000_000;

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        BigDecimal firstPrice = extractTotalPrice(response1);
        assertThat(firstPrice).isEqualByComparingTo(originalPrice);
        System.out.println("[1차 조회] 가격: " + firstPrice + ", 응답 시간: " + firstMs + "ms");

        // 2단계: DB에서 Rate 가격을 150,000으로 직접 UPDATE
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery(
                "UPDATE RateJpaEntity r SET r.basePrice = :newPrice WHERE r.id = :id")
                .setParameter("newPrice", newPrice)
                .setParameter("id", rateId)
                .executeUpdate();
            entityManager.flush();
        });

        // 3단계: Redis에서 해당 캐시 키 삭제 (가격 변경 시 발생하는 캐시 무효화 시뮬레이션)
        flushRedis();

        // 4단계: 2차 조회 → 가격 150,000 확인 (캐시 미스 → DB에서 새 가격 로드)
        long start2 = System.nanoTime();
        ResponseEntity<Map<String, Object>> response2 = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        long secondMs = (System.nanoTime() - start2) / 1_000_000;

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        BigDecimal secondPrice = extractTotalPrice(response2);
        assertThat(secondPrice).as("캐시 무효화 후 DB의 새 가격 150,000이 반영되어야 한다")
            .isEqualByComparingTo(newPrice);
        System.out.println("[2차 조회 - 캐시 미스] 가격: " + secondPrice + ", 응답 시간: " + secondMs + "ms");

        // 5단계: 3차 조회 → 가격 150,000 확인 (캐시 히트)
        long start3 = System.nanoTime();
        ResponseEntity<Map<String, Object>> response3 = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        long thirdMs = (System.nanoTime() - start3) / 1_000_000;

        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        BigDecimal thirdPrice = extractTotalPrice(response3);
        assertThat(thirdPrice).as("캐시 히트 시에도 새 가격 150,000이 유지되어야 한다")
            .isEqualByComparingTo(newPrice);
        System.out.println("[3차 조회 - 캐시 히트] 가격: " + thirdPrice + ", 응답 시간: " + thirdMs + "ms");

        // 캐시 히트가 캐시 미스보다 빠른지 확인 (참고 지표)
        System.out.println("[비교] 캐시 미스: " + secondMs + "ms → 캐시 히트: " + thirdMs + "ms");
    }

    /**
     * 요금 조회 응답에서 첫 번째 객실의 totalPrice를 추출한다.
     */
    private BigDecimal extractTotalPrice(ResponseEntity<Map<String, Object>> response) {
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
        assertThat(data).isNotEmpty();

        return new BigDecimal(data.get(0).get("totalPrice").toString());
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

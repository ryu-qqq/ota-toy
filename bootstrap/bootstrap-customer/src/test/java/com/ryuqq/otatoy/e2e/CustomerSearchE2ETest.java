package com.ryuqq.otatoy.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Customer 검색/요금 조회 E2E 테스트.
 * 숙소 검색(P0)과 요금 조회(P0) 시나리오를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("e2e")
@DisplayName("Customer 검색/요금 조회 E2E 테스트")
class CustomerSearchE2ETest extends CustomerE2ETestBase {

    private static final String SEARCH_URL = "/api/v1/search/properties";
    private static final String RATE_URL = "/api/v1/properties";

    @Nested
    @DisplayName("P0: 숙소 검색")
    class PropertySearch {

        @Test
        @DisplayName("Property + RoomType + Inventory 삽입 후 검색하면 200 + 결과에 숙소 포함")
        void shouldSearchPropertiesWithAvailableInventory() {
            // given: 사전 데이터 삽입
            Long partnerId = insertPartner("검색 테스트 파트너");
            Long propertyTypeId = insertPropertyType("HOTEL", "호텔");
            Long propertyId = insertProperty(partnerId, propertyTypeId, "검색 테스트 호텔");
            Long roomTypeId = insertRoomType(propertyId, "스탠다드");

            LocalDate checkIn = LocalDate.now().plusDays(30);
            LocalDate checkOut = checkIn.plusDays(2);

            // 체크인~체크아웃 기간 동안 재고 삽입
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                insertInventory(roomTypeId, date, 10, 10);
            }

            // when: 숙소 검색
            String url = String.format(
                "%s%s?checkIn=%s&checkOut=%s&region=서울&guests=2&size=20",
                baseUrl(), SEARCH_URL, checkIn, checkOut);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            // then: 200 응답 + 결과에 숙소 포함
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).isNotEmpty();

            // 검색 결과에 등록한 숙소가 포함되어 있는지 확인
            boolean found = content.stream()
                .anyMatch(item -> ((Number) item.get("propertyId")).longValue() == propertyId);
            assertThat(found).isTrue();
        }

        @Test
        @DisplayName("재고가 없는 지역으로 검색하면 빈 결과 반환")
        void shouldReturnEmptyWhenNoInventoryAvailable() {
            // given: 사전 데이터 삽입 (재고 없음)
            Long partnerId = insertPartner("빈 검색 파트너");
            Long propertyTypeId = insertPropertyType("MOTEL", "모텔");
            insertProperty(partnerId, propertyTypeId, "빈 재고 모텔");

            LocalDate checkIn = LocalDate.now().plusDays(60);
            LocalDate checkOut = checkIn.plusDays(1);

            // when: 존재하지 않는 지역으로 검색
            String url = String.format(
                "%s%s?checkIn=%s&checkOut=%s&region=부산&guests=2&size=20",
                baseUrl(), SEARCH_URL, checkIn, checkOut);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            // then: 200 응답 + 빈 결과
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).isEmpty();
        }
    }

    @Nested
    @DisplayName("P0: 요금 조회")
    class RateQuery {

        @Test
        @DisplayName("Property + RoomType + RatePlan + Rate + Inventory 삽입 후 요금 조회하면 200 + 요금 데이터 포함")
        void shouldReturnRatesForPropertyWithRatePlan() {
            // given: 사전 데이터 삽입
            Long partnerId = insertPartner("요금 조회 파트너");
            Long propertyTypeId = insertPropertyType("RESORT", "리조트");
            Long propertyId = insertProperty(partnerId, propertyTypeId, "요금 조회 리조트");
            Long roomTypeId = insertRoomType(propertyId, "디럭스");
            Long ratePlanId = insertRatePlan(roomTypeId, "기본 요금제");

            LocalDate checkIn = LocalDate.now().plusDays(30);
            LocalDate checkOut = checkIn.plusDays(2);
            BigDecimal price = new BigDecimal("120000.00");

            // 체크인~체크아웃 기간 동안 요금 + 재고 삽입
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                insertRateRule(ratePlanId, date, date.plusDays(1), price);
                insertRate(ratePlanId, date, price);
                insertInventory(roomTypeId, date, 10, 10);
            }

            // when: 요금 조회
            String url = String.format(
                "%s%s/%d/rates?checkIn=%s&checkOut=%s&guests=2",
                baseUrl(), RATE_URL, propertyId, checkIn, checkOut);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            // then: 200 응답 + 요금 데이터 포함
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
            assertThat(data).isNotEmpty();

            // 첫 번째 객실 요금 검증
            Map<String, Object> roomRate = data.get(0);
            assertThat(((Number) roomRate.get("roomTypeId")).longValue()).isEqualTo(roomTypeId);
            assertThat(roomRate.get("roomTypeName")).isEqualTo("디럭스");
            assertThat(((Number) roomRate.get("ratePlanId")).longValue()).isEqualTo(ratePlanId);
            assertThat(roomRate.get("ratePlanName")).isEqualTo("기본 요금제");

            // 날짜별 요금 리스트 검증
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dailyRates = (List<Map<String, Object>>) roomRate.get("dailyRates");
            assertThat(dailyRates).hasSize(2); // 2박

            // 총 가격 검증 (120000 * 2 = 240000)
            BigDecimal totalPrice = new BigDecimal(roomRate.get("totalPrice").toString());
            assertThat(totalPrice).isEqualByComparingTo(price.multiply(BigDecimal.valueOf(2)));
        }
    }
}

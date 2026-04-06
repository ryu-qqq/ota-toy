package com.ryuqq.otatoy.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Extranet API E2E 테스트.
 * 숙소 등록 -> 사진/편의시설/속성값 설정 -> 상세 조회,
 * 객실 등록 -> 요금 정책 등록 -> 요금/재고 설정,
 * 목록 조회 페이지네이션, 존재하지 않는 숙소 조회를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Tag("e2e")
@DisplayName("Extranet 숙소 관리 E2E 테스트")
class ExtranetPropertyE2ETest extends ExtranetE2ETestBase {

    private static final String PROPERTIES_URL = "/api/v1/extranet/properties";

    @Nested
    @DisplayName("P0: 숙소 전체 등록 흐름")
    class PropertyFullRegistrationFlow {

        @Test
        @DisplayName("숙소 등록 -> 사진 설정 -> 편의시설 설정 -> 속성값 설정 -> 상세 조회")
        void shouldRegisterPropertyWithFullDetails() {
            // given: 사전 데이터 삽입
            Long partnerId = insertPartner("테스트 파트너");
            Long propertyTypeId = insertPropertyType("HOTEL", "호텔");
            Long attrId = insertPropertyTypeAttribute(propertyTypeId, "star_rating", "성급");

            // 1단계: 숙소 기본정보 등록
            Map<String, Object> registerRequest = Map.ofEntries(
                Map.entry("partnerId", partnerId),
                Map.entry("propertyTypeId", propertyTypeId),
                Map.entry("name", "테스트 호텔"),
                Map.entry("description", "테스트용 호텔입니다"),
                Map.entry("address", "서울시 강남구 테헤란로 123"),
                Map.entry("latitude", 37.5065),
                Map.entry("longitude", 127.0536),
                Map.entry("neighborhood", "강남"),
                Map.entry("region", "서울")
            );

            ResponseEntity<Map<String, Object>> registerResponse = postJson(
                PROPERTIES_URL, registerRequest);

            assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Map<String, Object> registerBody = registerResponse.getBody();
            assertThat(registerBody).isNotNull();
            Long propertyId = ((Number) registerBody.get("data")).longValue();
            assertThat(propertyId).isPositive();

            // 2단계: 사진 설정
            Map<String, Object> photoRequest = Map.of(
                "photos", List.of(
                    Map.of("photoType", "EXTERIOR", "originUrl", "https://example.com/exterior.jpg",
                            "cdnUrl", "https://cdn.example.com/exterior.jpg", "sortOrder", 0),
                    Map.of("photoType", "LOBBY", "originUrl", "https://example.com/lobby.jpg",
                            "cdnUrl", "https://cdn.example.com/lobby.jpg", "sortOrder", 1)
                )
            );

            ResponseEntity<Map<String, Object>> photoResponse = putJson(
                PROPERTIES_URL + "/" + propertyId + "/photos", photoRequest);
            assertThat(photoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 3단계: 편의시설 설정
            Map<String, Object> amenityRequest = Map.of(
                "amenities", List.of(
                    Map.of("amenityType", "WIFI", "name", "무료 Wi-Fi",
                            "additionalPrice", 0, "sortOrder", 0),
                    Map.of("amenityType", "BATHTUB", "name", "욕조",
                            "additionalPrice", 0, "sortOrder", 1)
                )
            );

            ResponseEntity<Map<String, Object>> amenityResponse = putJson(
                PROPERTIES_URL + "/" + propertyId + "/amenities", amenityRequest);
            assertThat(amenityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 4단계: 속성값 설정
            Map<String, Object> attrRequest = Map.of(
                "attributes", List.of(
                    Map.of("propertyTypeAttributeId", attrId, "value", "5")
                )
            );

            ResponseEntity<Map<String, Object>> attrResponse = putJson(
                PROPERTIES_URL + "/" + propertyId + "/attributes", attrRequest);
            assertThat(attrResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 5단계: 상세 조회
            ResponseEntity<Map<String, Object>> detailResponse = restTemplate.exchange(
                baseUrl() + PROPERTIES_URL + "/" + propertyId,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, Object> detailBody = detailResponse.getBody();
            assertThat(detailBody).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) detailBody.get("data");
            assertThat(data.get("name")).isEqualTo("테스트 호텔");
            assertThat(data.get("address")).isEqualTo("서울시 강남구 테헤란로 123");

            // 사진, 편의시설, 속성값 리스트 크기 검증
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> photos = (List<Map<String, Object>>) data.get("photos");
            assertThat(photos).hasSize(2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> amenities = (List<Map<String, Object>>) data.get("amenities");
            assertThat(amenities).hasSize(2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attributeValues = (List<Map<String, Object>>) data.get("attributeValues");
            assertThat(attributeValues).hasSize(1);
        }
    }

    @Nested
    @DisplayName("P0: 객실 + 요금 정책 + 요금/재고 설정")
    class RoomTypeAndRatePlanFlow {

        @Test
        @DisplayName("객실 등록 -> 요금 정책 등록 -> 요금/재고 설정 전체 흐름")
        void shouldRegisterRoomTypeWithRatePlanAndRates() {
            // given: 사전 데이터 삽입
            Long partnerId = insertPartner("요금 테스트 파트너");
            Long propertyTypeId = insertPropertyType("PENSION", "펜션");
            Long propertyId = insertProperty(partnerId, propertyTypeId, "요금 테스트 펜션");

            // 1단계: 객실 등록
            Map<String, Object> roomRequest = Map.ofEntries(
                Map.entry("name", "디럭스 룸"),
                Map.entry("description", "넓은 디럭스 객실"),
                Map.entry("areaSqm", 45.5),
                Map.entry("areaPyeong", "13.8평"),
                Map.entry("baseOccupancy", 2),
                Map.entry("maxOccupancy", 4),
                Map.entry("baseInventory", 10),
                Map.entry("checkInTime", "15:00"),
                Map.entry("checkOutTime", "11:00"),
                Map.entry("beds", List.of(Map.of("bedTypeId", 1, "quantity", 1))),
                Map.entry("views", List.of(Map.of("viewTypeId", 1)))
            );

            ResponseEntity<Map<String, Object>> roomResponse = postJson(
                PROPERTIES_URL + "/" + propertyId + "/rooms", roomRequest);

            assertThat(roomResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long roomTypeId = ((Number) roomResponse.getBody().get("data")).longValue();
            assertThat(roomTypeId).isPositive();

            // 2단계: 요금 정책 등록
            Map<String, Object> ratePlanRequest = Map.of(
                "name", "기본 요금제",
                "freeCancellation", true,
                "nonRefundable", false,
                "freeCancellationDeadlineDays", 3,
                "cancellationPolicyText", "3일 전 무료 취소",
                "paymentPolicy", "PREPAY"
            );

            ResponseEntity<Map<String, Object>> ratePlanResponse = postJson(
                PROPERTIES_URL + "/" + propertyId + "/rooms/" + roomTypeId + "/rate-plans",
                ratePlanRequest);

            assertThat(ratePlanResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long ratePlanId = ((Number) ratePlanResponse.getBody().get("data")).longValue();
            assertThat(ratePlanId).isPositive();

            // 3단계: 요금/재고 설정
            LocalDate startDate = LocalDate.now().plusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(20);

            Map<String, Object> rateRequest = Map.ofEntries(
                Map.entry("startDate", startDate.toString()),
                Map.entry("endDate", endDate.toString()),
                Map.entry("basePrice", 100000),
                Map.entry("weekdayPrice", 90000),
                Map.entry("fridayPrice", 120000),
                Map.entry("saturdayPrice", 150000),
                Map.entry("sundayPrice", 80000),
                Map.entry("baseInventory", 5),
                Map.entry("overrides", List.of())
            );

            ResponseEntity<Map<String, Object>> rateResponse = putJson(
                "/api/v1/extranet/rate-plans/" + ratePlanId + "/rates", rateRequest);

            assertThat(rateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // DB 검증: rate_rule, inventory 레코드 생성 확인
            assertThat(rateRuleJpaRepository.count()).isGreaterThan(0);
            assertThat(inventoryJpaRepository.count()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("P1: 숙소 목록 조회 페이지네이션")
    class PropertyListPagination {

        @Test
        @DisplayName("숙소 3개 등록 후 size=2로 페이지네이션 조회")
        void shouldPaginatePropertyList() {
            // given: 파트너 + 숙소 유형 + 숙소 3개 등록
            Long partnerId = insertPartner("페이지네이션 파트너");
            Long propertyTypeId = insertPropertyType("MOTEL", "모텔");
            insertProperty(partnerId, propertyTypeId, "숙소 A");
            insertProperty(partnerId, propertyTypeId, "숙소 B");
            insertProperty(partnerId, propertyTypeId, "숙소 C");

            // 1페이지
            ResponseEntity<Map<String, Object>> page1 = restTemplate.exchange(
                baseUrl() + PROPERTIES_URL + "?partnerId=" + partnerId + "&size=2",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            assertThat(page1.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, Object> page1Body = page1.getBody();
            assertThat(page1Body).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> page1Data = (Map<String, Object>) page1Body.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> page1Content = (List<Map<String, Object>>) page1Data.get("content");
            assertThat(page1Content).hasSize(2);
            assertThat(page1Data.get("hasNext")).isEqualTo(true);
            assertThat(page1Data.get("nextCursor")).isNotNull();

            // 2페이지
            Object nextCursor = page1Data.get("nextCursor");
            ResponseEntity<Map<String, Object>> page2 = restTemplate.exchange(
                baseUrl() + PROPERTIES_URL + "?partnerId=" + partnerId + "&size=2&cursor=" + nextCursor,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            assertThat(page2.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked")
            Map<String, Object> page2Data = (Map<String, Object>) page2.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> page2Content = (List<Map<String, Object>>) page2Data.get("content");
            assertThat(page2Content).hasSize(1);
            assertThat(page2Data.get("hasNext")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("P1: 존재하지 않는 숙소 조회")
    class PropertyNotFound {

        @Test
        @DisplayName("존재하지 않는 숙소 ID로 상세 조회 시 404 응답")
        void shouldReturn404ForNonExistentProperty() {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl() + PROPERTIES_URL + "/999999",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            // RFC 7807 Problem Detail body 검증
            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("type")).isNotNull();
            assertThat(body.get("title")).isNotNull();
            assertThat(((Number) body.get("status")).intValue()).isEqualTo(404);
        }
    }

    // --- 공통 HTTP 헬퍼 ---

    private ResponseEntity<Map<String, Object>> postJson(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {});
    }

    private ResponseEntity<Map<String, Object>> putJson(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.PUT,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {});
    }
}

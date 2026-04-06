package com.ryuqq.otatoy.api.customer.rate;

import com.ryuqq.otatoy.api.customer.rate.dto.response.DailyRateApiResponse;
import com.ryuqq.otatoy.api.customer.rate.dto.response.RoomRateApiResponse;
import com.ryuqq.otatoy.api.customer.rate.mapper.RateApiMapper;
import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateApiMapper 단위 테스트.
 * Request -> Query, Result -> Response 변환 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class RateApiMapperTest {

    // =========================================================================
    // toQuery: (propertyId, checkIn, checkOut, guests) -> CustomerGetRateQuery
    // =========================================================================

    @Nested
    @DisplayName("toQuery - 파라미터 -> Query 변환")
    class ToQuery {

        @Test
        @DisplayName("propertyId가 PropertyId VO로 변환된다")
        void shouldConvertPropertyIdToVO() {
            CustomerGetRateQuery query = RateApiMapper.toQuery(
                1L,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                2
            );

            assertThat(query.propertyId()).isEqualTo(PropertyId.of(1L));
        }

        @Test
        @DisplayName("checkIn, checkOut, guests가 올바르게 매핑된다")
        void shouldMapAllParameters() {
            CustomerGetRateQuery query = RateApiMapper.toQuery(
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 18),
                3
            );

            assertThat(query.propertyId()).isEqualTo(PropertyId.of(10L));
            assertThat(query.checkIn()).isEqualTo(LocalDate.of(2026, 6, 15));
            assertThat(query.checkOut()).isEqualTo(LocalDate.of(2026, 6, 18));
            assertThat(query.guests()).isEqualTo(3);
        }
    }

    // =========================================================================
    // toApiResponse: RoomRateSummary -> RoomRateApiResponse
    // =========================================================================

    @Nested
    @DisplayName("toApiResponse - RoomRateSummary -> RoomRateApiResponse 변환")
    class ToApiResponse {

        @Test
        @DisplayName("객실 유형 정보가 올바르게 변환된다")
        void shouldMapRoomTypeInfo() {
            RoomRateSummary summary = createSummary();

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.roomTypeId()).isEqualTo(1L);
            assertThat(response.roomTypeName()).isEqualTo("디럭스 더블");
            assertThat(response.maxOccupancy()).isEqualTo(2);
        }

        @Test
        @DisplayName("요금 정책 정보가 올바르게 변환된다")
        void shouldMapRatePlanInfo() {
            RoomRateSummary summary = createSummary();

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.ratePlanId()).isEqualTo(10L);
            assertThat(response.ratePlanName()).isEqualTo("기본 요금");
        }

        @Test
        @DisplayName("취소 정책 필드가 올바르게 변환된다")
        void shouldMapCancellationPolicy() {
            RoomRateSummary summary = createSummary();

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.freeCancellation()).isTrue();
            assertThat(response.nonRefundable()).isFalse();
            assertThat(response.cancellationDeadlineDays()).isEqualTo(3);
        }

        @Test
        @DisplayName("총 가격이 올바르게 변환된다")
        void shouldMapTotalPrice() {
            RoomRateSummary summary = createSummary();

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(240000));
        }

        @Test
        @DisplayName("dailyRates 리스트가 올바르게 변환된다")
        void shouldMapDailyRates() {
            RoomRateSummary summary = createSummaryWithMultipleDays();

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.dailyRates()).hasSize(2);
            assertThat(response.dailyRates().get(0).date()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(response.dailyRates().get(0).basePrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));
            assertThat(response.dailyRates().get(0).availableCount()).isEqualTo(5);
            assertThat(response.dailyRates().get(0).available()).isTrue();
            assertThat(response.dailyRates().get(1).date()).isEqualTo(LocalDate.of(2026, 5, 2));
        }

        @Test
        @DisplayName("빈 dailyRates 리스트도 올바르게 변환된다")
        void shouldMapEmptyDailyRates() {
            RoomRateSummary summary = RoomRateSummary.of(
                RoomTypeId.of(1L), RoomTypeName.of("스탠다드"), 2,
                RatePlanId.of(10L), RatePlanName.of("기본 요금"),
                CancellationPolicy.of(false, false, 0, null),
                List.of(), BigDecimal.ZERO
            );

            RoomRateApiResponse response = RateApiMapper.toApiResponse(summary);

            assertThat(response.dailyRates()).isEmpty();
        }
    }

    // =========================================================================
    // toDailyRateResponse: DailyRate -> DailyRateApiResponse
    // =========================================================================

    @Nested
    @DisplayName("toDailyRateResponse - DailyRate -> DailyRateApiResponse 변환")
    class ToDailyRateResponse {

        @Test
        @DisplayName("날짜별 요금 정보가 올바르게 변환된다")
        void shouldMapDailyRate() {
            DailyRate dailyRate = DailyRate.of(
                LocalDate.of(2026, 5, 1),
                BigDecimal.valueOf(150000),
                3,
                true
            );

            DailyRateApiResponse response = RateApiMapper.toDailyRateResponse(dailyRate);

            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(response.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(150000));
            assertThat(response.availableCount()).isEqualTo(3);
            assertThat(response.available()).isTrue();
        }

        @Test
        @DisplayName("재고가 없는 경우 available이 false로 변환된다")
        void shouldMapUnavailableDailyRate() {
            DailyRate dailyRate = DailyRate.of(
                LocalDate.of(2026, 5, 1),
                BigDecimal.valueOf(150000),
                0,
                false
            );

            DailyRateApiResponse response = RateApiMapper.toDailyRateResponse(dailyRate);

            assertThat(response.availableCount()).isZero();
            assertThat(response.available()).isFalse();
        }
    }

    // =========================================================================
    // 헬퍼 메서드
    // =========================================================================

    private static RoomRateSummary createSummary() {
        DailyRate dailyRate = DailyRate.of(
            LocalDate.of(2026, 5, 1), BigDecimal.valueOf(120000), 5, true
        );
        return RoomRateSummary.of(
            RoomTypeId.of(1L), RoomTypeName.of("디럭스 더블"), 2,
            RatePlanId.of(10L), RatePlanName.of("기본 요금"),
            CancellationPolicy.of(true, false, 3, "3일 전 무료 취소"),
            List.of(dailyRate), BigDecimal.valueOf(240000)
        );
    }

    private static RoomRateSummary createSummaryWithMultipleDays() {
        DailyRate day1 = DailyRate.of(
            LocalDate.of(2026, 5, 1), BigDecimal.valueOf(120000), 5, true
        );
        DailyRate day2 = DailyRate.of(
            LocalDate.of(2026, 5, 2), BigDecimal.valueOf(120000), 3, true
        );
        return RoomRateSummary.of(
            RoomTypeId.of(1L), RoomTypeName.of("디럭스 더블"), 2,
            RatePlanId.of(10L), RatePlanName.of("기본 요금"),
            CancellationPolicy.of(true, false, 3, "3일 전 무료 취소"),
            List.of(day1, day2), BigDecimal.valueOf(240000)
        );
    }
}

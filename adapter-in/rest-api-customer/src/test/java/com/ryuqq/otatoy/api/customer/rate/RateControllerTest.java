package com.ryuqq.otatoy.api.customer.rate;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.PropertyRateResult;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.application.pricing.port.in.FetchRateUseCase;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RateController MockMvc 단위 테스트.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@WebMvcTest(RateController.class)
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class})
class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FetchRateUseCase fetchRateUseCase;

    @Test
    @DisplayName("요금 조회 성공 시 200 응답 + ApiResponse 구조 검증")
    void 요금_조회_성공시_200_응답() throws Exception {
        // given
        DailyRate dailyRate = DailyRate.of(
                LocalDate.of(2026, 5, 1),
                BigDecimal.valueOf(120000),
                5,
                true
        );

        RoomRateSummary roomRate = RoomRateSummary.of(
                RoomTypeId.of(1L),
                RoomTypeName.of("디럭스 더블"),
                2,
                RatePlanId.of(1L),
                RatePlanName.of("기본 요금"),
                CancellationPolicy.of(true, false, 3, "3일 전 무료 취소"),
                List.of(dailyRate),
                BigDecimal.valueOf(120000)
        );

        PropertyRateResult result = PropertyRateResult.of(
                PropertyId.of(1L),
                List.of(roomRate)
        );

        given(fetchRateUseCase.execute(any())).willReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/properties/1/rates")
                        .param("checkIn", "2026-05-01")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomTypeId").value(1))
                .andExpect(jsonPath("$.data[0].roomTypeName").value("디럭스 더블"))
                .andExpect(jsonPath("$.data[0].maxOccupancy").value(2))
                .andExpect(jsonPath("$.data[0].ratePlanName").value("기본 요금"))
                .andExpect(jsonPath("$.data[0].freeCancellation").value(true))
                .andExpect(jsonPath("$.data[0].totalPrice").value(120000))
                .andExpect(jsonPath("$.data[0].dailyRates[0].date").value("2026-05-01"))
                .andExpect(jsonPath("$.data[0].dailyRates[0].basePrice").value(120000))
                .andExpect(jsonPath("$.data[0].dailyRates[0].available").value(true));
    }

    @Test
    @DisplayName("요금 조회 시 객실이 없으면 빈 목록 반환")
    void 요금_조회시_객실_없으면_빈_목록() throws Exception {
        // given
        PropertyRateResult result = PropertyRateResult.of(
                PropertyId.of(1L),
                List.of()
        );

        given(fetchRateUseCase.execute(any())).willReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/properties/1/rates")
                        .param("checkIn", "2026-05-01")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("체크인 날짜 누락 시 400 응답")
    void 체크인_날짜_누락시_400_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/properties/1/rates")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("체크아웃 날짜 누락 시 400 응답")
    void 체크아웃_날짜_누락시_400_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/properties/1/rates")
                        .param("checkIn", "2026-05-01")
                        .param("guests", "2"))
                .andExpect(status().isBadRequest());
    }
}

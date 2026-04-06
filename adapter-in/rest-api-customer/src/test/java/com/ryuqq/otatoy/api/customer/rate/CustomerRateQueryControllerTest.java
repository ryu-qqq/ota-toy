package com.ryuqq.otatoy.api.customer.rate;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.customer.common.error.CustomerPropertyErrorMapper;
import com.ryuqq.otatoy.api.customer.fixture.CustomerRateFixture;
import com.ryuqq.otatoy.api.customer.rate.controller.CustomerRateQueryController;
import com.ryuqq.otatoy.application.pricing.port.in.CustomerGetRateUseCase;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CustomerRateQueryController 테스트.
 * GET 엔드포인트의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(CustomerRateQueryController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, CustomerPropertyErrorMapper.class})
class CustomerRateQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerGetRateUseCase customerGetRateUseCase;

    // =========================================================================
    // GET /api/v1/properties/{propertyId}/rates -- 요금 조회
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/properties/{propertyId}/rates -- 요금 조회")
    class GetRates {

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_조회() throws Exception {
            given(customerGetRateUseCase.execute(any()))
                .willReturn(CustomerRateFixture.rateResult());

            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkIn", CustomerRateFixture.CHECK_IN)
                    .param("checkOut", CustomerRateFixture.CHECK_OUT)
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomTypeName").value("디럭스 더블"))
                .andExpect(jsonPath("$.data[0].freeCancellation").value(true))
                .andExpect(jsonPath("$.data[0].totalPrice").value(120000))
                .andExpect(jsonPath("$.data[0].dailyRates[0].date").value("2026-05-01"))
                .andExpect(jsonPath("$.data[0].dailyRates[0].available").value(true))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("customer-get-rates",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    queryParameters(
                        parameterWithName("checkIn").description("체크인 날짜 (필수, yyyy-MM-dd)"),
                        parameterWithName("checkOut").description("체크아웃 날짜 (필수, yyyy-MM-dd)"),
                        parameterWithName("guests").description("투숙 인원 (1 이상)")
                    ),
                    responseFields(
                        fieldWithPath("data[]").description("객실별 요금 목록"),
                        fieldWithPath("data[].roomTypeId").description("객실 유형 ID"),
                        fieldWithPath("data[].roomTypeName").description("객실 유형 이름"),
                        fieldWithPath("data[].maxOccupancy").description("최대 수용 인원"),
                        fieldWithPath("data[].ratePlanId").description("요금제 ID"),
                        fieldWithPath("data[].ratePlanName").description("요금제 이름"),
                        fieldWithPath("data[].freeCancellation").description("무료 취소 가능 여부"),
                        fieldWithPath("data[].nonRefundable").description("환불 불가 여부"),
                        fieldWithPath("data[].cancellationDeadlineDays").description("무료 취소 기한 (일)"),
                        fieldWithPath("data[].dailyRates[]").description("날짜별 요금 목록"),
                        fieldWithPath("data[].dailyRates[].date").description("숙박 날짜"),
                        fieldWithPath("data[].dailyRates[].basePrice").description("기본 가격"),
                        fieldWithPath("data[].dailyRates[].availableCount").description("잔여 재고 수"),
                        fieldWithPath("data[].dailyRates[].available").description("예약 가능 여부"),
                        fieldWithPath("data[].totalPrice").description("총 금액"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("객실이 없으면 빈 목록 반환")
        void 빈_결과() throws Exception {
            given(customerGetRateUseCase.execute(any()))
                .willReturn(CustomerRateFixture.emptyRateResult());

            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkIn", CustomerRateFixture.CHECK_IN)
                    .param("checkOut", CustomerRateFixture.CHECK_OUT)
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("체크인 날짜 누락 시 400 Bad Request")
        void 체크인_누락() throws Exception {
            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkOut", CustomerRateFixture.CHECK_OUT)
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andDo(document("customer-get-rates-validation-error",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("type").description("에러 유형 URI"),
                        fieldWithPath("title").description("에러 제목"),
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("detail").description("에러 상세 메시지"),
                        fieldWithPath("instance").description("요청 URI"),
                        fieldWithPath("timestamp").description("발생 시각"),
                        fieldWithPath("code").description("에러 코드"),
                        subsectionWithPath("errors").description("필드별 에러 목록").optional(),
                        fieldWithPath("traceId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("체크아웃 날짜 누락 시 400 Bad Request")
        void 체크아웃_누락() throws Exception {
            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkIn", CustomerRateFixture.CHECK_IN)
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("guests=0 전달 시 기본값 1로 보정되어 200 OK")
        void guests_0이면_기본값_보정() throws Exception {
            given(customerGetRateUseCase.execute(any()))
                .willReturn(CustomerRateFixture.emptyRateResult());

            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkIn", CustomerRateFixture.CHECK_IN)
                    .param("checkOut", CustomerRateFixture.CHECK_OUT)
                    .param("guests", "0"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("체크아웃이 체크인보다 앞일 때 400 Bad Request")
        void 체크아웃이_체크인보다_앞으면_예외() throws Exception {
            given(customerGetRateUseCase.execute(any()))
                .willThrow(new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다"));

            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates",
                        CustomerRateFixture.PROPERTY_ID)
                    .param("checkIn", "2026-05-05")
                    .param("checkOut", "2026-05-03")
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
        }

        @Test
        @DisplayName("존재하지 않는 숙소 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            given(customerGetRateUseCase.execute(any()))
                .willThrow(new PropertyNotFoundException());

            mockMvc.perform(get(CustomerRateEndpoints.BASE + "/{propertyId}/rates", 999L)
                    .param("checkIn", CustomerRateFixture.CHECK_IN)
                    .param("checkOut", CustomerRateFixture.CHECK_OUT)
                    .param("guests", CustomerRateFixture.GUESTS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andDo(document("customer-get-rates-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }
    }
}

package com.ryuqq.otatoy.api.extranet.pricing;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.api.extranet.fixture.ExtranetRatePlanFixture;
import com.ryuqq.otatoy.api.extranet.pricing.controller.ExtranetRatePlanCommandController;
import com.ryuqq.otatoy.application.pricing.port.in.RegisterRatePlanUseCase;
import com.ryuqq.otatoy.application.pricing.port.in.SetRateAndInventoryUseCase;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetRatePlanCommandController 테스트.
 * 요금 정책 등록 및 요금/재고 설정 API의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetRatePlanCommandController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetRatePlanCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterRatePlanUseCase registerRatePlanUseCase;

    @MockitoBean
    SetRateAndInventoryUseCase setRateAndInventoryUseCase;

    // =========================================================================
    // POST /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans -- 요금 정책 등록")
    class RegisterRatePlan {

        private static final String URL = "/api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans";

        @Test
        @DisplayName("유효한 요청 시 201 Created")
        void 정상_등록() throws Exception {
            given(registerRatePlanUseCase.execute(any())).willReturn(200L);

            mockMvc.perform(post(URL, 1L, 10L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.registerRatePlanRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(200))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-register-rate-plan",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID"),
                        parameterWithName("roomTypeId").description("객실 유형 ID")
                    ),
                    requestFields(
                        fieldWithPath("name").description("요금 정책명 (필수, 200자 이하)"),
                        fieldWithPath("freeCancellation").description("무료 취소 가능 여부 (선택)").optional(),
                        fieldWithPath("nonRefundable").description("환불 불가 여부 (선택)").optional(),
                        fieldWithPath("freeCancellationDeadlineDays").description("무료 취소 마감일수 (선택)").optional(),
                        fieldWithPath("cancellationPolicyText").description("취소 정책 설명 (선택, 2000자 이하)").optional(),
                        fieldWithPath("paymentPolicy").description("결제 정책 (필수, 예: PREPAID, POSTPAID)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("생성된 요금 정책 ID"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(post(URL, 1L, 10L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.registerRatePlanInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andDo(document("extranet-register-rate-plan-validation-error",
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
                        subsectionWithPath("errors").description("필드별 에러 목록"),
                        fieldWithPath("traceId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("존재하지 않는 객실 유형 시 404 Not Found")
        void 객실유형_미존재() throws Exception {
            given(registerRatePlanUseCase.execute(any()))
                .willThrow(new RoomTypeNotFoundException());

            mockMvc.perform(post(URL, 1L, 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.registerRatePlanRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-002"))
                .andDo(document("extranet-register-rate-plan-room-type-not-found",
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
                        fieldWithPath("traceId").description("요청 추적 ID")
                    )
                ));
        }
    }

    // =========================================================================
    // PUT /api/v1/extranet/rate-plans/{ratePlanId}/rates -- 요금/재고 설정
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/rate-plans/{ratePlanId}/rates -- 요금/재고 설정")
    class SetRateAndInventory {

        private static final String URL = "/api/v1/extranet/rate-plans/{ratePlanId}/rates";

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_설정() throws Exception {
            willDoNothing().given(setRateAndInventoryUseCase).execute(any());

            mockMvc.perform(put(URL, 200L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.setRateAndInventoryRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-set-rate-and-inventory",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("ratePlanId").description("요금 정책 ID")
                    ),
                    requestFields(
                        fieldWithPath("startDate").description("시작일 (필수, yyyy-MM-dd)"),
                        fieldWithPath("endDate").description("종료일 (필수, yyyy-MM-dd)"),
                        fieldWithPath("basePrice").description("기본 요금 (필수)"),
                        fieldWithPath("weekdayPrice").description("평일 요금 (선택)").optional(),
                        fieldWithPath("fridayPrice").description("금요일 요금 (선택)").optional(),
                        fieldWithPath("saturdayPrice").description("토요일 요금 (선택)").optional(),
                        fieldWithPath("sundayPrice").description("일요일 요금 (선택)").optional(),
                        fieldWithPath("baseInventory").description("기본 재고 (0 이상)"),
                        fieldWithPath("overrides[]").description("날짜별 요금 오버라이드 (선택)").optional(),
                        fieldWithPath("overrides[].date").description("오버라이드 날짜 (필수)"),
                        fieldWithPath("overrides[].price").description("오버라이드 요금 (필수)"),
                        fieldWithPath("overrides[].reason").description("오버라이드 사유 (선택)").optional()
                    ),
                    responseFields(
                        fieldWithPath("data").description("응답 데이터 (void 시 null)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(put(URL, 200L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.setRateAndInventoryInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andDo(document("extranet-set-rate-and-inventory-validation-error",
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
                        subsectionWithPath("errors").description("필드별 에러 목록"),
                        fieldWithPath("traceId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("baseInventory 음수일 때 400 Bad Request (@Min(0) 위반)")
        void baseInventory_음수일때_400() throws Exception {
            mockMvc.perform(put(URL, 200L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.setRateAndInventoryNegativeInventoryRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.baseInventory").exists());
        }

        @Test
        @DisplayName("overrides 내부 price = null일 때 400 Bad Request (@NotNull 위반)")
        void overrides_price_null일때_400() throws Exception {
            mockMvc.perform(put(URL, 200L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.setRateAndInventoryOverridePriceNullRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("존재하지 않는 요금 정책 시 404 Not Found")
        void 요금정책_미존재() throws Exception {
            willThrow(new RatePlanNotFoundException())
                .given(setRateAndInventoryUseCase).execute(any());

            mockMvc.perform(put(URL, 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRatePlanFixture.setRateAndInventoryRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRC-001"))
                .andDo(document("extranet-set-rate-and-inventory-not-found",
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
                        fieldWithPath("traceId").description("요청 추적 ID")
                    )
                ));
        }
    }
}

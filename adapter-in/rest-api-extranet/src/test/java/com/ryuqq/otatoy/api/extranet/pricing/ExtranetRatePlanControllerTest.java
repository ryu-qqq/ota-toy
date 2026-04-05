package com.ryuqq.otatoy.api.extranet.pricing;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.application.pricing.port.in.RegisterRatePlanUseCase;
import com.ryuqq.otatoy.application.pricing.port.in.SetRateAndInventoryUseCase;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetRatePlanController MockMvc 테스트.
 * 요금 정책(RatePlan) 등록 API의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetRatePlanController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetRatePlanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterRatePlanUseCase registerRatePlanUseCase;

    @MockitoBean
    SetRateAndInventoryUseCase setRateAndInventoryUseCase;

    private static final String BASE_URL =
        "/api/v1/extranet/properties/1/rooms/2/rate-plans";

    private static final String VALID_REQUEST_BODY = """
        {
            "name": "기본 요금 정책",
            "freeCancellation": true,
            "nonRefundable": false,
            "freeCancellationDeadlineDays": 3,
            "cancellationPolicyText": "체크인 3일 전까지 무료 취소 가능합니다",
            "paymentPolicy": "PREPAY"
        }
        """;

    // =====================================================================
    // POST /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans
    // =====================================================================

    @Nested
    @DisplayName("POST /api/v1/extranet/properties/{propertyId}/rooms/{roomTypeId}/rate-plans")
    class RegisterRatePlan {

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 요금 정책 등록 시 201 Created와 ratePlanId를 반환한다")
            void shouldReturnCreatedWithRatePlanId() throws Exception {
                // given
                given(registerRatePlanUseCase.execute(any()))
                    .willReturn(100L);

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(100))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("extranet-register-rate-plan",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                            fieldWithPath("name").description("요금 정책명"),
                            fieldWithPath("freeCancellation").description("무료 취소 가능 여부").optional(),
                            fieldWithPath("nonRefundable").description("환불 불가 여부").optional(),
                            fieldWithPath("freeCancellationDeadlineDays").description("무료 취소 마감일(일)").optional(),
                            fieldWithPath("cancellationPolicyText").description("취소 정책 설명").optional(),
                            fieldWithPath("paymentPolicy").description("결제 정책 (PREPAY, PAY_AT_PROPERTY, PAY_BEFORE_CHECKIN)")
                        ),
                        responseFields(
                            fieldWithPath("success").description("성공 여부"),
                            fieldWithPath("data").description("생성된 요금 정책 ID"),
                            fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                    ));
            }

            @Test
            @DisplayName("선택 필드 없이 필수 필드만으로도 등록 성공한다")
            void shouldSucceedWithOnlyRequiredFields() throws Exception {
                // given
                given(registerRatePlanUseCase.execute(any()))
                    .willReturn(200L);

                String minimalRequest = """
                    {
                        "name": "최소 요금 정책",
                        "paymentPolicy": "PAY_AT_PROPERTY"
                    }
                    """;

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(minimalRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(200));
            }
        }

        @Nested
        @DisplayName("AIT-2: Validation 실패")
        class ValidationFail {

            @Test
            @DisplayName("name이 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenNameIsBlank() throws Exception {
                String body = """
                    {
                        "name": "",
                        "paymentPolicy": "PREPAY"
                    }
                    """;

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("name이 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenNameIsNull() throws Exception {
                String body = """
                    {
                        "paymentPolicy": "PREPAY"
                    }
                    """;

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("name이 200자를 초과하면 400 Bad Request를 반환한다")
            void shouldReturn400WhenNameExceedsMaxLength() throws Exception {
                String longName = "가".repeat(201);
                String body = """
                    {
                        "name": "%s",
                        "paymentPolicy": "PREPAY"
                    }
                    """.formatted(longName);

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("paymentPolicy가 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenPaymentPolicyIsNull() throws Exception {
                String body = """
                    {
                        "name": "테스트 요금 정책"
                    }
                    """;

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("cancellationPolicyText가 2000자를 초과하면 400 Bad Request를 반환한다")
            void shouldReturn400WhenCancellationPolicyTextExceedsMaxLength() throws Exception {
                String longText = "가".repeat(2001);
                String body = """
                    {
                        "name": "테스트 요금 정책",
                        "paymentPolicy": "PREPAY",
                        "cancellationPolicyText": "%s"
                    }
                    """.formatted(longText);

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }
        }

        @Nested
        @DisplayName("AIT-3 / AIT-4: UseCase 예외 매핑")
        class UseCaseException {

            @Test
            @DisplayName("RoomTypeNotFoundException 발생 시 404 Not Found를 반환한다")
            void shouldReturn404WhenRoomTypeNotFound() throws Exception {
                // given
                given(registerRatePlanUseCase.execute(any()))
                    .willThrow(new RoomTypeNotFoundException());

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("ACC-002"))
                    .andExpect(jsonPath("$.error.userMessage").value("객실 유형을 찾을 수 없습니다"));
            }
        }

        @Nested
        @DisplayName("AIT-5: 응답 포맷 일관성")
        class ResponseFormat {

            @Test
            @DisplayName("성공 응답은 { success: true, data: N, error: null } 포맷이다")
            void successResponseShouldHaveCorrectFormat() throws Exception {
                // given
                given(registerRatePlanUseCase.execute(any()))
                    .willReturn(1L);

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andExpect(jsonPath("$.error").doesNotExist());
            }

            @Test
            @DisplayName("실패 응답은 { success: false, data: null, error: { code, userMessage, debugMessage } } 포맷이다")
            void errorResponseShouldHaveCorrectFormat() throws Exception {
                // given
                given(registerRatePlanUseCase.execute(any()))
                    .willThrow(new RoomTypeNotFoundException());

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").isMap())
                    .andExpect(jsonPath("$.error.code").isString())
                    .andExpect(jsonPath("$.error.userMessage").isString())
                    .andExpect(jsonPath("$.error.debugMessage").isString());
            }
        }
    }
}

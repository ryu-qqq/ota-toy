package com.ryuqq.otatoy.api.extranet.roomtype;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;
import com.ryuqq.otatoy.application.roomtype.port.in.RegisterRoomTypeUseCase;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.InvalidRoomTypeException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetRoomTypeController MockMvc 테스트.
 * 객실 유형 등록 API의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetRoomTypeController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetRoomTypeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterRoomTypeUseCase registerRoomTypeUseCase;

    private static final String BASE_URL = ExtranetPropertyEndpoints.PROPERTIES + "/1/rooms";

    private static final String VALID_REQUEST_BODY = """
        {
            "name": "디럭스 더블",
            "description": "넓은 창이 있는 디럭스 더블룸",
            "areaSqm": 33.5,
            "areaPyeong": "10.1",
            "baseOccupancy": 2,
            "maxOccupancy": 3,
            "baseInventory": 10,
            "checkInTime": "15:00",
            "checkOutTime": "11:00",
            "beds": [
                {
                    "bedTypeId": 1,
                    "quantity": 1
                }
            ],
            "views": [
                {
                    "viewTypeId": 1
                }
            ]
        }
        """;

    @Nested
    @DisplayName("POST /api/v1/extranet/properties/{propertyId}/rooms")
    class RegisterRoomType {

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 객실 등록 시 201 Created와 roomTypeId를 반환한다")
            void shouldReturnCreatedWithRoomTypeId() throws Exception {
                // given
                given(registerRoomTypeUseCase.execute(any()))
                    .willReturn(5L);

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(5))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("extranet-roomtype-register",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
            }

            @Test
            @DisplayName("선택 필드(description, beds, views) 없이도 등록 성공한다")
            void shouldSucceedWithoutOptionalFields() throws Exception {
                // given
                given(registerRoomTypeUseCase.execute(any()))
                    .willReturn(10L);

                String minimalRequest = """
                    {
                        "name": "스탠다드",
                        "areaSqm": 20.0,
                        "baseOccupancy": 2,
                        "maxOccupancy": 2,
                        "baseInventory": 5,
                        "checkInTime": "15:00",
                        "checkOutTime": "11:00"
                    }
                    """;

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(minimalRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(10));
            }
        }

        @Nested
        @DisplayName("AIT-2: Validation 실패")
        class ValidationFail {

            @Test
            @DisplayName("name이 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenNameIsBlank() throws Exception {
                String body = """
                    {
                        "name": "",
                        "areaSqm": 33.5,
                        "baseOccupancy": 2,
                        "maxOccupancy": 3,
                        "baseInventory": 10,
                        "checkInTime": "15:00",
                        "checkOutTime": "11:00"
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
            @DisplayName("name이 100자 초과하면 400 Bad Request를 반환한다")
            void shouldReturn400WhenNameExceedsMaxLength() throws Exception {
                String longName = "가".repeat(101);
                String body = """
                    {
                        "name": "%s",
                        "areaSqm": 33.5,
                        "baseOccupancy": 2,
                        "maxOccupancy": 3,
                        "baseInventory": 10,
                        "checkInTime": "15:00",
                        "checkOutTime": "11:00"
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
            @DisplayName("areaSqm이 null이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAreaSqmIsNull() throws Exception {
                String body = """
                    {
                        "name": "디럭스 더블",
                        "baseOccupancy": 2,
                        "maxOccupancy": 3,
                        "baseInventory": 10,
                        "checkInTime": "15:00",
                        "checkOutTime": "11:00"
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
            @DisplayName("checkInTime이 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenCheckInTimeIsBlank() throws Exception {
                String body = """
                    {
                        "name": "디럭스 더블",
                        "areaSqm": 33.5,
                        "baseOccupancy": 2,
                        "maxOccupancy": 3,
                        "baseInventory": 10,
                        "checkInTime": "",
                        "checkOutTime": "11:00"
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
            @DisplayName("checkOutTime이 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenCheckOutTimeIsBlank() throws Exception {
                String body = """
                    {
                        "name": "디럭스 더블",
                        "areaSqm": 33.5,
                        "baseOccupancy": 2,
                        "maxOccupancy": 3,
                        "baseInventory": 10,
                        "checkInTime": "15:00",
                        "checkOutTime": ""
                    }
                    """;

                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }
        }

        @Nested
        @DisplayName("AIT-3: 존재하지 않는 리소스")
        class NotFound {

            @Test
            @DisplayName("PropertyNotFoundException 발생 시 404 Not Found를 반환한다")
            void shouldReturn404WhenPropertyNotFound() throws Exception {
                // given
                given(registerRoomTypeUseCase.execute(any()))
                    .willThrow(new PropertyNotFoundException());

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("숙소를 찾을 수 없습니다"));
            }
        }

        @Nested
        @DisplayName("AIT-4: baseOccupancy > maxOccupancy 도메인 검증")
        class OccupancyValidation {

            @Test
            @DisplayName("baseOccupancy > maxOccupancy이면 400 Bad Request를 반환한다")
            void shouldReturnErrorWhenBaseExceedsMaxOccupancy() throws Exception {
                // given — 도메인에서 InvalidRoomTypeException(DomainException) 발생 → 400 매핑
                given(registerRoomTypeUseCase.execute(any()))
                    .willThrow(new InvalidRoomTypeException("최대 인원은 기본 인원 이상이어야 합니다"));

                String body = """
                    {
                        "name": "디럭스 더블",
                        "areaSqm": 33.5,
                        "baseOccupancy": 5,
                        "maxOccupancy": 2,
                        "baseInventory": 10,
                        "checkInTime": "15:00",
                        "checkOutTime": "11:00"
                    }
                    """;

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-005"));
            }
        }

        @Nested
        @DisplayName("AIT-5: 응답 포맷 일관성")
        class ResponseFormat {

            @Test
            @DisplayName("성공 응답은 { success: true, data: N, error: null } 포맷이다")
            void successResponseShouldHaveCorrectFormat() throws Exception {
                // given
                given(registerRoomTypeUseCase.execute(any()))
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
                given(registerRoomTypeUseCase.execute(any()))
                    .willThrow(new PropertyNotFoundException());

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

package com.ryuqq.otatoy.api.extranet.roomtype;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.api.extranet.fixture.ExtranetRoomTypeFixture;
import com.ryuqq.otatoy.api.extranet.roomtype.controller.ExtranetRoomTypeCommandController;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetRoomTypeCommandController 테스트.
 * 객실 유형 등록 API의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetRoomTypeCommandController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetRoomTypeCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterRoomTypeUseCase registerRoomTypeUseCase;

    @Nested
    @DisplayName("POST /api/v1/extranet/properties/{propertyId}/rooms -- 객실 유형 등록")
    class RegisterRoomType {

        @Test
        @DisplayName("유효한 요청 시 201 Created")
        void 정상_등록() throws Exception {
            given(registerRoomTypeUseCase.execute(any())).willReturn(100L);

            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(100))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-register-room-type",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    requestFields(
                        fieldWithPath("name").description("객실 이름 (필수, 100자 이하)"),
                        fieldWithPath("description").description("객실 설명 (선택, 2000자 이하)").optional(),
                        fieldWithPath("areaSqm").description("면적 - 평방미터 (필수, 0 이상)"),
                        fieldWithPath("areaPyeong").description("면적 - 평 (선택)").optional(),
                        fieldWithPath("baseOccupancy").description("기본 투숙 인원 (1 이상)"),
                        fieldWithPath("maxOccupancy").description("최대 투숙 인원 (1 이상)"),
                        fieldWithPath("baseInventory").description("기본 재고 (0 이상)"),
                        fieldWithPath("checkInTime").description("체크인 시간 (HH:mm 형식, 필수)"),
                        fieldWithPath("checkOutTime").description("체크아웃 시간 (HH:mm 형식, 필수)"),
                        fieldWithPath("beds[]").description("침대 구성 (선택)").optional(),
                        fieldWithPath("beds[].bedTypeId").description("침대 유형 ID"),
                        fieldWithPath("beds[].quantity").description("침대 수량 (1 이상)"),
                        fieldWithPath("views[]").description("전망 (선택)").optional(),
                        fieldWithPath("views[].viewTypeId").description("전망 유형 ID")
                    ),
                    responseFields(
                        fieldWithPath("data").description("생성된 객실 유형 ID"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("최소 필수 필드만으로 201 Created")
        void 최소_필드_등록() throws Exception {
            given(registerRoomTypeUseCase.execute(any())).willReturn(101L);

            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeMinimalRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(101));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andDo(document("extranet-register-room-type-validation-error",
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
        @DisplayName("baseOccupancy = 0일 때 400 Bad Request (@Positive 위반)")
        void baseOccupancy_0일때_400() throws Exception {
            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeBaseOccupancyZeroRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.baseOccupancy").exists());
        }

        @Test
        @DisplayName("maxOccupancy < baseOccupancy일 때 도메인 예외로 400 Bad Request")
        void maxOccupancy_미만일때_400() throws Exception {
            given(registerRoomTypeUseCase.execute(any()))
                .willThrow(new InvalidRoomTypeException("최대 인원은 기본 인원 이상이어야 합니다"));

            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeMaxLessThanBaseRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ACC-005"));
        }

        @Test
        @DisplayName("areaSqm 음수일 때 400 Bad Request (@PositiveOrZero 위반)")
        void areaSqm_음수일때_400() throws Exception {
            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeNegativeAreaRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.areaSqm").exists());
        }

        @Test
        @DisplayName("beds 내부 quantity = 0일 때 400 Bad Request (@Positive 위반)")
        void beds_quantity_0일때_400() throws Exception {
            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeBedQuantityZeroRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['beds[0].quantity']").exists());
        }

        @Test
        @DisplayName("checkInTime 유효하지 않은 포맷 (25:00) 시 400 Bad Request")
        void checkInTime_잘못된_포맷() throws Exception {
            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeInvalidCheckInTimeRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATETIME_FORMAT"));
        }

        @Test
        @DisplayName("존재하지 않는 숙소 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            given(registerRoomTypeUseCase.execute(any()))
                .willThrow(new PropertyNotFoundException());

            mockMvc.perform(post(ExtranetRoomTypeEndpoints.ROOMS, 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetRoomTypeFixture.registerRoomTypeRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andDo(document("extranet-register-room-type-not-found",
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

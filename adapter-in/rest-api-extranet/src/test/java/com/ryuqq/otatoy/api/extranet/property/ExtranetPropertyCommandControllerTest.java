package com.ryuqq.otatoy.api.extranet.property;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.api.extranet.fixture.ExtranetPropertyFixture;
import com.ryuqq.otatoy.api.extranet.property.controller.ExtranetPropertyCommandController;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyPhotosUseCase;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.RequiredPropertyAttributeMissingException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

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
 * ExtranetPropertyCommandController 테스트.
 * POST/PUT 엔드포인트의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetPropertyCommandController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetPropertyCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterPropertyUseCase registerPropertyUseCase;

    @MockitoBean
    SetPropertyPhotosUseCase setPropertyPhotosUseCase;

    @MockitoBean
    SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase;

    @MockitoBean
    SetPropertyAttributesUseCase setPropertyAttributesUseCase;

    // =========================================================================
    // POST /api/v1/extranet/properties -- 숙소 등록
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/extranet/properties -- 숙소 등록")
    class RegisterProperty {

        @Test
        @DisplayName("유효한 요청 시 201 Created")
        void 정상_등록() throws Exception {
            given(registerPropertyUseCase.execute(any())).willReturn(42L);

            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(42))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists())
                .andDo(document("extranet-register-property",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("partnerId").description("파트너 ID (필수)"),
                        fieldWithPath("brandId").description("브랜드 ID (선택)"),
                        fieldWithPath("propertyTypeId").description("숙소 유형 ID (필수)"),
                        fieldWithPath("name").description("숙소 이름 (필수, 100자 이하)"),
                        fieldWithPath("description").description("숙소 설명 (선택, 2000자 이하)"),
                        fieldWithPath("address").description("주소 (필수)"),
                        fieldWithPath("latitude").description("위도"),
                        fieldWithPath("longitude").description("경도"),
                        fieldWithPath("neighborhood").description("인근 지역 (선택)"),
                        fieldWithPath("region").description("지역 (선택)"),
                        fieldWithPath("promotionText").description("홍보 문구 (선택, 500자 이하)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("생성된 숙소 ID"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("최소 필수 필드만으로 201 Created")
        void 최소_필드_등록() throws Exception {
            given(registerPropertyUseCase.execute(any())).willReturn(43L);

            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyMinimalRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(43));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").value("입력값이 올바르지 않습니다"))
                .andDo(document("extranet-register-property-validation-error",
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
        @DisplayName("name이 정확히 100자일 때 201 Created (경계값 성공)")
        void name_100자_경계값_성공() throws Exception {
            given(registerPropertyUseCase.execute(any())).willReturn(44L);

            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyName100Request()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(44));
        }

        @Test
        @DisplayName("name이 101자일 때 400 Bad Request (경계값 실패)")
        void name_101자_경계값_실패() throws Exception {
            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyName101Request()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("존재하지 않는 파트너 시 404 Not Found")
        void 파트너_미존재() throws Exception {
            given(registerPropertyUseCase.execute(any()))
                .willThrow(new PartnerNotFoundException());

            mockMvc.perform(post(ExtranetPropertyEndpoints.PROPERTIES)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.registerPropertyRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PTN-001"))
                .andDo(document("extranet-register-property-partner-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }
    }

    // =========================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/photos -- 사진 설정
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/photos -- 사진 설정")
    class SetPhotos {

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_설정() throws Exception {
            willDoNothing().given(setPropertyPhotosUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/photos", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setPhotosRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists())
                .andDo(document("extranet-set-photos",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    requestFields(
                        fieldWithPath("photos[]").description("사진 목록 (필수, 1건 이상)"),
                        fieldWithPath("photos[].photoType").description("사진 유형 코드 (EXTERIOR, LOBBY, ROOM 등)"),
                        fieldWithPath("photos[].originUrl").description("원본 이미지 URL (필수)"),
                        fieldWithPath("photos[].cdnUrl").description("CDN 이미지 URL (선택)").optional(),
                        fieldWithPath("photos[].sortOrder").description("정렬 순서 (0 이상)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("응답 데이터 (void 시 null)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("빈 사진 목록 시 400 Bad Request")
        void 빈_목록() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/photos", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setPhotosEmptyRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("sortOrder 음수일 때 400 Bad Request")
        void sortOrder_음수() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/photos", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setPhotosSortOrderNegativeRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['photos[0].sortOrder']").exists());
        }

        @Test
        @DisplayName("존재하지 않는 숙소 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            willThrow(new PropertyNotFoundException())
                .given(setPropertyPhotosUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/photos", 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setPhotosRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andDo(document("extranet-set-photos-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }
    }

    // =========================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/amenities -- 편의시설 설정
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/amenities -- 편의시설 설정")
    class SetAmenities {

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_설정() throws Exception {
            willDoNothing().given(setPropertyAmenitiesUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/amenities", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAmenitiesRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-set-amenities",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    requestFields(
                        fieldWithPath("amenities[]").description("편의시설 목록 (필수)"),
                        fieldWithPath("amenities[].amenityType").description("편의시설 유형 코드 (PARKING, POOL 등)"),
                        fieldWithPath("amenities[].name").description("편의시설 이름 (필수)"),
                        fieldWithPath("amenities[].additionalPrice").description("추가 요금 (0이면 무료)"),
                        fieldWithPath("amenities[].sortOrder").description("정렬 순서 (0 이상)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("응답 데이터 (void 시 null)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("amenities 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/amenities", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAmenitiesInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("additionalPrice 음수일 때 400 Bad Request")
        void additionalPrice_음수() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/amenities", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAmenitiesNegativePriceRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['amenities[0].additionalPrice']").exists());
        }

        @Test
        @DisplayName("존재하지 않는 숙소 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            willThrow(new PropertyNotFoundException())
                .given(setPropertyAmenitiesUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/amenities", 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAmenitiesRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andDo(document("extranet-set-amenities-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }
    }

    // =========================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/attributes -- 속성값 설정
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/attributes -- 속성값 설정")
    class SetAttributes {

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_설정() throws Exception {
            willDoNothing().given(setPropertyAttributesUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/attributes", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAttributesRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-set-attributes",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    requestFields(
                        fieldWithPath("attributes[]").description("속성값 목록 (필수, 1건 이상)"),
                        fieldWithPath("attributes[].propertyTypeAttributeId").description("숙소유형 속성 정의 ID (필수)"),
                        fieldWithPath("attributes[].value").description("속성값 (필수)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("응답 데이터 (void 시 null)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("빈 속성값 목록 시 400 Bad Request")
        void 빈_목록() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/attributes", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAttributesEmptyRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("value가 빈 문자열일 때 400 Bad Request")
        void value_빈문자열() throws Exception {
            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/attributes", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAttributesBlankValueRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['attributes[0].value']").exists());
        }

        @Test
        @DisplayName("존재하지 않는 숙소 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            willThrow(new PropertyNotFoundException())
                .given(setPropertyAttributesUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/attributes", 999L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAttributesRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andDo(document("extranet-set-attributes-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }

        @Test
        @DisplayName("필수 속성 누락 도메인 예외 시 400 Bad Request")
        void 필수_속성_누락() throws Exception {
            willThrow(new RequiredPropertyAttributeMissingException(
                    Set.of(PropertyTypeAttributeId.of(10L))))
                .given(setPropertyAttributesUseCase).execute(any());

            mockMvc.perform(put(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}/attributes", 1L)
                    .contentType(APPLICATION_JSON)
                    .content(ExtranetPropertyFixture.setAttributesRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ACC-006"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andDo(document("extranet-set-attributes-missing-required",
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

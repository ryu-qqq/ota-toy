package com.ryuqq.otatoy.api.extranet.property;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyPhotosUseCase;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.RequiredPropertyAttributeMissingException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetPropertyController MockMvc 테스트.
 * 숙소 기본정보 등록, 사진 추가, 편의시설 설정, 속성값 설정 API의
 * 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetPropertyController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetPropertyControllerTest {

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

    private static final String BASE_URL = ExtranetPropertyEndpoints.PROPERTIES;

    private static final String VALID_REQUEST_BODY = """
        {
            "partnerId": 1,
            "brandId": 10,
            "propertyTypeId": 2,
            "name": "테스트 호텔",
            "description": "서울 강남에 위치한 테스트 호텔입니다",
            "address": "서울특별시 강남구 테헤란로 123",
            "latitude": 37.5012,
            "longitude": 127.0396,
            "neighborhood": "강남역",
            "region": "서울",
            "promotionText": "오픈 기념 특가"
        }
        """;

    // =====================================================================
    // POST /api/v1/extranet/properties — 숙소 기본정보 등록
    // =====================================================================

    @Nested
    @DisplayName("POST /api/v1/extranet/properties")
    class RegisterProperty {

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 숙소 등록 시 201 Created와 propertyId를 반환한다")
            void shouldReturnCreatedWithPropertyId() throws Exception {
                // given
                given(registerPropertyUseCase.execute(any()))
                    .willReturn(42L);

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(42))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("register-property",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                            fieldWithPath("partnerId").description("파트너 ID"),
                            fieldWithPath("propertyTypeId").description("숙소 유형 ID"),
                            fieldWithPath("name").description("숙소명"),
                            fieldWithPath("address").description("주소"),
                            fieldWithPath("latitude").description("위도"),
                            fieldWithPath("longitude").description("경도"),
                            fieldWithPath("brandId").description("브랜드 ID").optional(),
                            fieldWithPath("description").description("설명").optional(),
                            fieldWithPath("neighborhood").description("동네").optional(),
                            fieldWithPath("region").description("지역").optional(),
                            fieldWithPath("promotionText").description("프로모션 문구").optional()
                        ),
                        responseFields(
                            fieldWithPath("success").description("성공 여부"),
                            fieldWithPath("data").description("생성된 숙소 ID"),
                            fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                    ));
            }

            @Test
            @DisplayName("선택 필드(brandId, description, promotionText) 없이도 등록 성공한다")
            void shouldSucceedWithoutOptionalFields() throws Exception {
                // given
                given(registerPropertyUseCase.execute(any()))
                    .willReturn(100L);

                String minimalRequest = """
                    {
                        "partnerId": 1,
                        "propertyTypeId": 2,
                        "name": "최소 정보 호텔",
                        "address": "서울특별시 중구 을지로 1",
                        "latitude": 37.5660,
                        "longitude": 126.9784
                    }
                    """;

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(minimalRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(100));
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
                        "partnerId": 1,
                        "propertyTypeId": 2,
                        "name": "",
                        "address": "서울특별시 강남구 테헤란로 123",
                        "latitude": 37.5012,
                        "longitude": 127.0396
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
            @DisplayName("partnerId가 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenPartnerIdIsNull() throws Exception {
                String body = """
                    {
                        "propertyTypeId": 2,
                        "name": "테스트 호텔",
                        "address": "서울특별시 강남구 테헤란로 123",
                        "latitude": 37.5012,
                        "longitude": 127.0396
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
            @DisplayName("propertyTypeId가 누락되면 400 Bad Request를 반환한다")
            void shouldReturn400WhenPropertyTypeIdIsNull() throws Exception {
                String body = """
                    {
                        "partnerId": 1,
                        "name": "테스트 호텔",
                        "address": "서울특별시 강남구 테헤란로 123",
                        "latitude": 37.5012,
                        "longitude": 127.0396
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
            @DisplayName("address가 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAddressIsBlank() throws Exception {
                String body = """
                    {
                        "partnerId": 1,
                        "propertyTypeId": 2,
                        "name": "테스트 호텔",
                        "address": "",
                        "latitude": 37.5012,
                        "longitude": 127.0396
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
                        "partnerId": 1,
                        "propertyTypeId": 2,
                        "name": "%s",
                        "address": "서울특별시 강남구 테헤란로 123",
                        "latitude": 37.5012,
                        "longitude": 127.0396
                    }
                    """.formatted(longName);

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
            @DisplayName("PartnerNotFoundException 발생 시 404 Not Found를 반환한다")
            void shouldReturn404WhenPartnerNotFound() throws Exception {
                // given
                given(registerPropertyUseCase.execute(any()))
                    .willThrow(new PartnerNotFoundException());

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("PTN-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("파트너를 찾을 수 없습니다"));
            }

            @Test
            @DisplayName("PropertyTypeNotFoundException 발생 시 404 Not Found를 반환한다")
            void shouldReturn404WhenPropertyTypeNotFound() throws Exception {
                // given
                given(registerPropertyUseCase.execute(any()))
                    .willThrow(new PropertyTypeNotFoundException());

                // when & then
                mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error.code").value("PT-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("숙소 유형을 찾을 수 없습니다"));
            }
        }

        @Nested
        @DisplayName("AIT-5: 응답 포맷 일관성")
        class ResponseFormat {

            @Test
            @DisplayName("성공 응답은 { success: true, data: N, error: null } 포맷이다")
            void successResponseShouldHaveCorrectFormat() throws Exception {
                // given
                given(registerPropertyUseCase.execute(any()))
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
                given(registerPropertyUseCase.execute(any()))
                    .willThrow(new PartnerNotFoundException());

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

    // =====================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/photos — 사진 설정
    // =====================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/photos")
    class SetPhotos {

        private static final String PHOTOS_URL = BASE_URL + "/1/photos";

        private static final String VALID_PHOTOS_BODY = """
            {
                "photos": [
                    {
                        "photoType": "EXTERIOR",
                        "originUrl": "https://example.com/photo1.jpg",
                        "cdnUrl": "https://cdn.example.com/photo1.jpg",
                        "sortOrder": 0
                    },
                    {
                        "photoType": "LOBBY",
                        "originUrl": "https://example.com/photo2.jpg",
                        "sortOrder": 1
                    }
                ]
            }
            """;

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 사진 설정 시 200 OK를 반환한다")
            void shouldReturnOkOnSuccess() throws Exception {
                // given
                willDoNothing().given(setPropertyPhotosUseCase).execute(any());

                // when & then
                mockMvc.perform(put(PHOTOS_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_PHOTOS_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("extranet-property-set-photos",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
            }
        }

        @Nested
        @DisplayName("AIT-2: Validation 실패")
        class ValidationFail {

            @Test
            @DisplayName("photos가 빈 배열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenPhotosIsEmpty() throws Exception {
                String body = """
                    {
                        "photos": []
                    }
                    """;

                mockMvc.perform(put(PHOTOS_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("photoType이 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenPhotoTypeIsBlank() throws Exception {
                String body = """
                    {
                        "photos": [
                            {
                                "photoType": "",
                                "originUrl": "https://example.com/photo.jpg",
                                "sortOrder": 0
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(PHOTOS_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("originUrl이 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenOriginUrlIsBlank() throws Exception {
                String body = """
                    {
                        "photos": [
                            {
                                "photoType": "EXTERIOR",
                                "originUrl": "",
                                "sortOrder": 0
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(PHOTOS_URL)
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
                willThrow(new PropertyNotFoundException())
                    .given(setPropertyPhotosUseCase).execute(any());

                // when & then
                mockMvc.perform(put(PHOTOS_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_PHOTOS_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("숙소를 찾을 수 없습니다"));
            }
        }
    }

    // =====================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/amenities — 편의시설 설정
    // =====================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/amenities")
    class SetAmenities {

        private static final String AMENITIES_URL = BASE_URL + "/1/amenities";

        private static final String VALID_AMENITIES_BODY = """
            {
                "amenities": [
                    {
                        "amenityType": "WIFI",
                        "name": "무료 Wi-Fi",
                        "additionalPrice": 0,
                        "sortOrder": 0
                    },
                    {
                        "amenityType": "BATHTUB",
                        "name": "욕조",
                        "additionalPrice": 5000,
                        "sortOrder": 1
                    }
                ]
            }
            """;

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 편의시설 설정 시 200 OK를 반환한다")
            void shouldReturnOkOnSuccess() throws Exception {
                // given
                willDoNothing().given(setPropertyAmenitiesUseCase).execute(any());

                // when & then
                mockMvc.perform(put(AMENITIES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_AMENITIES_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("extranet-property-set-amenities",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
            }
        }

        @Nested
        @DisplayName("AIT-2: Validation 실패")
        class ValidationFail {

            @Test
            @DisplayName("amenities가 null이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAmenitiesIsNull() throws Exception {
                String body = """
                    {}
                    """;

                mockMvc.perform(put(AMENITIES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("amenityType이 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAmenityTypeIsBlank() throws Exception {
                String body = """
                    {
                        "amenities": [
                            {
                                "amenityType": "",
                                "name": "무료 Wi-Fi",
                                "additionalPrice": 0,
                                "sortOrder": 0
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(AMENITIES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("name이 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAmenityNameIsBlank() throws Exception {
                String body = """
                    {
                        "amenities": [
                            {
                                "amenityType": "WIFI",
                                "name": "",
                                "additionalPrice": 0,
                                "sortOrder": 0
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(AMENITIES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("additionalPrice가 음수이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAdditionalPriceIsNegative() throws Exception {
                String body = """
                    {
                        "amenities": [
                            {
                                "amenityType": "WIFI",
                                "name": "무료 Wi-Fi",
                                "additionalPrice": -1000,
                                "sortOrder": 0
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(AMENITIES_URL)
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
                willThrow(new PropertyNotFoundException())
                    .given(setPropertyAmenitiesUseCase).execute(any());

                // when & then
                mockMvc.perform(put(AMENITIES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_AMENITIES_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("숙소를 찾을 수 없습니다"));
            }
        }
    }

    // =====================================================================
    // PUT /api/v1/extranet/properties/{propertyId}/attributes — 속성값 설정
    // =====================================================================

    @Nested
    @DisplayName("PUT /api/v1/extranet/properties/{propertyId}/attributes")
    class SetAttributes {

        private static final String ATTRIBUTES_URL = BASE_URL + "/1/attributes";

        private static final String VALID_ATTRIBUTES_BODY = """
            {
                "attributes": [
                    {
                        "propertyTypeAttributeId": 10,
                        "value": "5성급"
                    },
                    {
                        "propertyTypeAttributeId": 20,
                        "value": "2020"
                    }
                ]
            }
            """;

        @Nested
        @DisplayName("AIT-1: 정상 요청")
        class Success {

            @Test
            @DisplayName("유효한 요청으로 속성값 설정 시 200 OK를 반환한다")
            void shouldReturnOkOnSuccess() throws Exception {
                // given
                willDoNothing().given(setPropertyAttributesUseCase).execute(any());

                // when & then
                mockMvc.perform(put(ATTRIBUTES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_ATTRIBUTES_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andDo(document("extranet-property-set-attributes",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
            }
        }

        @Nested
        @DisplayName("AIT-2: Validation 실패")
        class ValidationFail {

            @Test
            @DisplayName("attributes가 빈 배열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAttributesIsEmpty() throws Exception {
                String body = """
                    {
                        "attributes": []
                    }
                    """;

                mockMvc.perform(put(ATTRIBUTES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("propertyTypeAttributeId가 null이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenAttributeIdIsNull() throws Exception {
                String body = """
                    {
                        "attributes": [
                            {
                                "value": "5성급"
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(ATTRIBUTES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("value가 빈 문자열이면 400 Bad Request를 반환한다")
            void shouldReturn400WhenValueIsBlank() throws Exception {
                String body = """
                    {
                        "attributes": [
                            {
                                "propertyTypeAttributeId": 10,
                                "value": ""
                            }
                        ]
                    }
                    """;

                mockMvc.perform(put(ATTRIBUTES_URL)
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
                willThrow(new PropertyNotFoundException())
                    .given(setPropertyAttributesUseCase).execute(any());

                // when & then
                mockMvc.perform(put(ATTRIBUTES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_ATTRIBUTES_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-001"))
                    .andExpect(jsonPath("$.error.userMessage").value("숙소를 찾을 수 없습니다"));
            }
        }

        @Nested
        @DisplayName("AIT-4: DomainException 매핑 — 필수 속성 누락")
        class RequiredAttributeMissing {

            @Test
            @DisplayName("필수 속성 누락 시 400 Bad Request를 반환한다")
            void shouldReturn400WhenRequiredAttributeIsMissing() throws Exception {
                // given
                willThrow(new RequiredPropertyAttributeMissingException(
                        java.util.Set.of(PropertyTypeAttributeId.of(99L))))
                    .given(setPropertyAttributesUseCase).execute(any());

                // when & then
                mockMvc.perform(put(ATTRIBUTES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(VALID_ATTRIBUTES_BODY))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ACC-006"))
                    .andExpect(jsonPath("$.error.userMessage").value("필수 속성이 누락되었습니다"));
            }
        }
    }
}

package com.ryuqq.otatoy.api.extranet.property;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.extranet.common.error.ExtranetPropertyErrorMapper;
import com.ryuqq.otatoy.api.extranet.fixture.ExtranetPropertyFixture;
import com.ryuqq.otatoy.api.extranet.property.controller.ExtranetPropertyQueryController;
import com.ryuqq.otatoy.application.property.port.in.ExtranetSearchPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.GetPropertyDetailUseCase;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExtranetPropertyQueryController 테스트.
 * GET 엔드포인트의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(ExtranetPropertyQueryController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, ExtranetPropertyErrorMapper.class})
class ExtranetPropertyQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ExtranetSearchPropertyUseCase extranetSearchPropertyUseCase;

    @MockitoBean
    GetPropertyDetailUseCase getPropertyDetailUseCase;

    @Nested
    @DisplayName("GET /api/v1/extranet/properties -- 숙소 목록 조회")
    class SearchProperties {

        @Test
        @DisplayName("유효한 요청 시 200 OK + 목록 반환")
        void 정상_목록_조회() throws Exception {
            given(extranetSearchPropertyUseCase.execute(any()))
                .willReturn(ExtranetPropertyFixture.sliceResult());

            mockMvc.perform(get(ExtranetPropertyEndpoints.PROPERTIES)
                    .param("partnerId", "1")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(2))
                .andExpect(jsonPath("$.data.content[0].propertyId").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("테스트 호텔"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-search-properties",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    queryParameters(
                        parameterWithName("partnerId").description("파트너 ID (필수)"),
                        parameterWithName("size").description("페이지 크기 (기본 20)").optional(),
                        parameterWithName("cursor").description("커서 (다음 페이지 조회 시)").optional()
                    ),
                    responseFields(
                        fieldWithPath("data.content[]").description("숙소 목록"),
                        fieldWithPath("data.content[].propertyId").description("숙소 ID"),
                        fieldWithPath("data.content[].name").description("숙소 이름"),
                        fieldWithPath("data.content[].propertyTypeId").description("숙소 유형 ID"),
                        fieldWithPath("data.content[].address").description("주소"),
                        fieldWithPath("data.content[].latitude").description("위도"),
                        fieldWithPath("data.content[].longitude").description("경도"),
                        fieldWithPath("data.content[].neighborhood").description("인근 지역"),
                        fieldWithPath("data.content[].region").description("지역"),
                        fieldWithPath("data.content[].status").description("숙소 상태"),
                        fieldWithPath("data.content[].lowestPrice").description("최저 요금"),
                        fieldWithPath("data.content[].createdAt").description("생성일시"),
                        fieldWithPath("data.content[].updatedAt").description("수정일시"),
                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                        fieldWithPath("data.nextCursor").description("다음 페이지 커서"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("빈 결과 시 200 OK + 빈 목록 반환")
        void 빈_목록_조회() throws Exception {
            given(extranetSearchPropertyUseCase.execute(any()))
                .willReturn(ExtranetPropertyFixture.emptySliceResult());

            mockMvc.perform(get(ExtranetPropertyEndpoints.PROPERTIES)
                    .param("partnerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
        }

        @Test
        @DisplayName("partnerId 누락 시 에러 응답")
        void partnerId_누락() throws Exception {
            mockMvc.perform(get(ExtranetPropertyEndpoints.PROPERTIES))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/extranet/properties/{propertyId} -- 숙소 상세 조회")
    class GetPropertyDetail {

        @Test
        @DisplayName("존재하는 숙소 ID 시 200 OK + 상세 반환")
        void 정상_상세_조회() throws Exception {
            given(getPropertyDetailUseCase.execute(any()))
                .willReturn(ExtranetPropertyFixture.propertyDetail());

            mockMvc.perform(get(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.propertyId").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 호텔"))
                .andExpect(jsonPath("$.data.photos").isArray())
                .andExpect(jsonPath("$.data.photos.length()").value(1))
                .andExpect(jsonPath("$.data.amenities").isArray())
                .andExpect(jsonPath("$.data.amenities.length()").value(1))
                .andExpect(jsonPath("$.data.attributeValues").isArray())
                .andExpect(jsonPath("$.data.attributeValues.length()").value(1))
                .andExpect(jsonPath("$.data.roomTypes").isArray())
                .andExpect(jsonPath("$.data.roomTypes.length()").value(1))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("extranet-property-detail",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
                    responseFields(
                        fieldWithPath("data.propertyId").description("숙소 ID"),
                        fieldWithPath("data.partnerId").description("파트너 ID"),
                        fieldWithPath("data.brandId").description("브랜드 ID"),
                        fieldWithPath("data.propertyTypeId").description("숙소 유형 ID"),
                        fieldWithPath("data.name").description("숙소 이름"),
                        fieldWithPath("data.description").description("숙소 설명"),
                        fieldWithPath("data.address").description("주소"),
                        fieldWithPath("data.latitude").description("위도"),
                        fieldWithPath("data.longitude").description("경도"),
                        fieldWithPath("data.neighborhood").description("인근 지역"),
                        fieldWithPath("data.region").description("지역"),
                        fieldWithPath("data.promotionText").description("프로모션 문구"),
                        fieldWithPath("data.status").description("숙소 상태"),
                        fieldWithPath("data.createdAt").description("생성일시"),
                        fieldWithPath("data.updatedAt").description("수정일시"),
                        fieldWithPath("data.photos[]").description("사진 목록"),
                        fieldWithPath("data.photos[].id").description("사진 ID"),
                        fieldWithPath("data.photos[].photoType").description("사진 유형"),
                        fieldWithPath("data.photos[].originUrl").description("원본 URL"),
                        fieldWithPath("data.photos[].cdnUrl").description("CDN URL"),
                        fieldWithPath("data.photos[].sortOrder").description("정렬 순서"),
                        fieldWithPath("data.amenities[]").description("편의시설 목록"),
                        fieldWithPath("data.amenities[].id").description("편의시설 ID"),
                        fieldWithPath("data.amenities[].amenityType").description("편의시설 유형"),
                        fieldWithPath("data.amenities[].name").description("편의시설 이름"),
                        fieldWithPath("data.amenities[].additionalPrice").description("추가 요금"),
                        fieldWithPath("data.amenities[].sortOrder").description("정렬 순서"),
                        fieldWithPath("data.attributeValues[]").description("속성값 목록"),
                        fieldWithPath("data.attributeValues[].id").description("속성값 ID"),
                        fieldWithPath("data.attributeValues[].propertyTypeAttributeId").description("속성 정의 ID"),
                        fieldWithPath("data.attributeValues[].value").description("속성값"),
                        fieldWithPath("data.roomTypes[]").description("객실 유형 목록"),
                        fieldWithPath("data.roomTypes[].roomTypeId").description("객실 유형 ID"),
                        fieldWithPath("data.roomTypes[].name").description("객실 이름"),
                        fieldWithPath("data.roomTypes[].description").description("객실 설명"),
                        fieldWithPath("data.roomTypes[].areaSqm").description("면적(평방미터)"),
                        fieldWithPath("data.roomTypes[].areaPyeong").description("면적(평)"),
                        fieldWithPath("data.roomTypes[].baseOccupancy").description("기본 투숙 인원"),
                        fieldWithPath("data.roomTypes[].maxOccupancy").description("최대 투숙 인원"),
                        fieldWithPath("data.roomTypes[].baseInventory").description("기본 재고"),
                        fieldWithPath("data.roomTypes[].checkInTime").description("체크인 시간"),
                        fieldWithPath("data.roomTypes[].checkOutTime").description("체크아웃 시간"),
                        fieldWithPath("data.roomTypes[].status").description("객실 상태"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("존재하지 않는 숙소 ID 시 404 Not Found")
        void 숙소_미존재() throws Exception {
            given(getPropertyDetailUseCase.execute(any()))
                .willThrow(new PropertyNotFoundException());

            mockMvc.perform(get(ExtranetPropertyEndpoints.PROPERTIES + "/{propertyId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACC-001"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andDo(document("extranet-property-detail-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("propertyId").description("숙소 ID")
                    ),
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

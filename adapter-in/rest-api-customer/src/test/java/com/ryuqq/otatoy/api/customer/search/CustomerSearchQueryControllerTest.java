package com.ryuqq.otatoy.api.customer.search;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.customer.common.error.CustomerPropertyErrorMapper;
import com.ryuqq.otatoy.api.customer.fixture.CustomerSearchFixture;
import com.ryuqq.otatoy.api.customer.search.controller.CustomerSearchQueryController;
import com.ryuqq.otatoy.application.property.port.in.CustomerSearchPropertyUseCase;
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
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CustomerSearchQueryController 테스트.
 * GET 엔드포인트의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(CustomerSearchQueryController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, CustomerPropertyErrorMapper.class})
class CustomerSearchQueryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerSearchPropertyUseCase customerSearchPropertyUseCase;

    // =========================================================================
    // GET /api/v1/search/properties -- 숙소 검색
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/search/properties -- 숙소 검색")
    class SearchProperties {

        @Test
        @DisplayName("유효한 요청 시 200 OK")
        void 정상_검색() throws Exception {
            given(customerSearchPropertyUseCase.execute(any()))
                .willReturn(CustomerSearchFixture.sliceResult());

            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", CustomerSearchFixture.SIZE)
                    .param("region", CustomerSearchFixture.REGION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].propertyId").value(1))
                .andExpect(jsonPath("$.data.content[0].lowestPrice").value(80000))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(2))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("customer-search-properties",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    queryParameters(
                        parameterWithName("checkIn").description("체크인 날짜 (필수, yyyy-MM-dd)"),
                        parameterWithName("checkOut").description("체크아웃 날짜 (필수, yyyy-MM-dd)"),
                        parameterWithName("guests").description("투숙 인원 (1 이상)"),
                        parameterWithName("size").description("페이지 크기 (1 이상, 기본 20)"),
                        parameterWithName("region").description("지역 필터 (선택)")
                    ),
                    responseFields(
                        fieldWithPath("data.content[]").description("숙소 목록"),
                        fieldWithPath("data.content[].propertyId").description("숙소 ID"),
                        fieldWithPath("data.content[].name").description("숙소 이름"),
                        fieldWithPath("data.content[].propertyTypeId").description("숙소 유형 ID"),
                        fieldWithPath("data.content[].address").description("주소"),
                        fieldWithPath("data.content[].latitude").description("위도"),
                        fieldWithPath("data.content[].longitude").description("경도"),
                        fieldWithPath("data.content[].region").description("지역"),
                        fieldWithPath("data.content[].lowestPrice").description("최저 가격"),
                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                        fieldWithPath("data.nextCursor").description("다음 페이지 커서"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("검색 결과가 비어있으면 빈 목록 반환")
        void 빈_결과() throws Exception {
            given(customerSearchPropertyUseCase.execute(any()))
                .willReturn(CustomerSearchFixture.emptySliceResult());

            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", CustomerSearchFixture.SIZE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));
        }

        @Test
        @DisplayName("체크인 날짜 누락 시 400 Bad Request")
        void 체크인_누락() throws Exception {
            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", CustomerSearchFixture.SIZE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andDo(document("customer-search-properties-validation-error",
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
            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", CustomerSearchFixture.SIZE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("guests=0 전달 시 기본값 1로 보정되어 200 OK")
        void guests_0이면_기본값_보정() throws Exception {
            given(customerSearchPropertyUseCase.execute(any()))
                .willReturn(CustomerSearchFixture.emptySliceResult());

            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", "0")
                    .param("size", CustomerSearchFixture.SIZE))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("size=0 전달 시 기본값 20으로 보정되어 200 OK")
        void size_0이면_기본값_보정() throws Exception {
            given(customerSearchPropertyUseCase.execute(any()))
                .willReturn(CustomerSearchFixture.emptySliceResult());

            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", "0"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("amenityTypes에 유효하지 않은 값 전달 시 400 Bad Request")
        void amenityTypes_유효하지않은값_예외() throws Exception {
            mockMvc.perform(get(CustomerSearchEndpoints.PROPERTIES)
                    .param("checkIn", CustomerSearchFixture.CHECK_IN)
                    .param("checkOut", CustomerSearchFixture.CHECK_OUT)
                    .param("guests", CustomerSearchFixture.GUESTS)
                    .param("size", CustomerSearchFixture.SIZE)
                    .param("amenityTypes", "INVALID_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
        }
    }
}

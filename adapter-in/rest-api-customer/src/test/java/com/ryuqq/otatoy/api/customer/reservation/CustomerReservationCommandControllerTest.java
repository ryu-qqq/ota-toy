package com.ryuqq.otatoy.api.customer.reservation;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.api.customer.fixture.CustomerReservationFixture;
import com.ryuqq.otatoy.api.customer.reservation.controller.CustomerReservationCommandController;
import com.ryuqq.otatoy.application.reservation.port.in.CancelReservationUseCase;
import com.ryuqq.otatoy.application.reservation.port.in.ConfirmReservationUseCase;
import com.ryuqq.otatoy.application.reservation.port.in.CreateReservationSessionUseCase;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.reservation.ReservationAlreadyCancelledException;
import com.ryuqq.otatoy.domain.reservation.ReservationNotFoundException;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionExpiredException;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionNotFoundException;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CustomerReservationCommandController 테스트.
 * POST/PATCH 엔드포인트의 요청/응답 포맷, HTTP 상태 코드, 에러 핸들링을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@WebMvcTest(CustomerReservationCommandController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class, CustomerReservationErrorMapper.class})
class CustomerReservationCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CreateReservationSessionUseCase createReservationSessionUseCase;

    @MockitoBean
    ConfirmReservationUseCase confirmReservationUseCase;

    @MockitoBean
    CancelReservationUseCase cancelReservationUseCase;

    // =========================================================================
    // POST /api/v1/reservation-sessions -- 예약 세션 생성
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/reservation-sessions -- 예약 세션 생성")
    class CreateSession {

        @Test
        @DisplayName("유효한 요청 시 201 Created")
        void 정상_생성() throws Exception {
            given(createReservationSessionUseCase.execute(any()))
                .willReturn(CustomerReservationFixture.sessionResult());

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .header("Idempotency-Key", CustomerReservationFixture.IDEMPOTENCY_KEY)
                    .content(CustomerReservationFixture.createSessionRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(240000))
                .andExpect(jsonPath("$.data.guestCount").value(2))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-06-01 10:00:00"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("customer-create-session",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName("Idempotency-Key").description("멱등성 키 (필수)")
                    ),
                    requestFields(
                        fieldWithPath("propertyId").description("숙소 ID (필수)"),
                        fieldWithPath("roomTypeId").description("객실 유형 ID (필수)"),
                        fieldWithPath("ratePlanId").description("요금제 ID (필수)"),
                        fieldWithPath("checkIn").description("체크인 날짜 (필수, 미래)"),
                        fieldWithPath("checkOut").description("체크아웃 날짜 (필수, 미래)"),
                        fieldWithPath("guestCount").description("투숙 인원 (1 이상)"),
                        fieldWithPath("totalAmount").description("총 금액 (필수)")
                    ),
                    responseFields(
                        fieldWithPath("data.sessionId").description("생성된 세션 ID"),
                        fieldWithPath("data.totalAmount").description("총 금액"),
                        fieldWithPath("data.checkIn").description("체크인 날짜"),
                        fieldWithPath("data.checkOut").description("체크아웃 날짜"),
                        fieldWithPath("data.guestCount").description("투숙 인원"),
                        fieldWithPath("data.expiresAt").description("세션 만료 시각 (yyyy-MM-dd HH:mm:ss)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .header("Idempotency-Key", CustomerReservationFixture.IDEMPOTENCY_KEY)
                    .content(CustomerReservationFixture.createSessionInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andDo(document("customer-create-session-validation-error",
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
        @DisplayName("재고 소진 시 409 Conflict")
        void 재고_소진() throws Exception {
            given(createReservationSessionUseCase.execute(any()))
                .willThrow(new InventoryExhaustedException());

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .header("Idempotency-Key", CustomerReservationFixture.IDEMPOTENCY_KEY)
                    .content(CustomerReservationFixture.createSessionRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INV-002"))
                .andDo(document("customer-create-session-inventory-exhausted",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }

        @Test
        @DisplayName("Idempotency-Key 헤더 누락 시 400 Bad Request")
        void 멱등키_누락() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.createSessionRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_HEADER"));
        }

        @Test
        @DisplayName("guestCount=0 전달 시 400 Bad Request (@Min 위반)")
        void guestCount_0이면_검증실패() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .header("Idempotency-Key", CustomerReservationFixture.IDEMPOTENCY_KEY)
                    .content("""
                        {
                            "propertyId": 1,
                            "roomTypeId": 1,
                            "ratePlanId": 1,
                            "checkIn": "2026-06-01",
                            "checkOut": "2026-06-03",
                            "guestCount": 0,
                            "totalAmount": 240000
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("checkIn이 과거 날짜이면 400 Bad Request (@Future 위반)")
        void checkIn_과거날짜_검증실패() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATION_SESSIONS)
                    .contentType(APPLICATION_JSON)
                    .header("Idempotency-Key", CustomerReservationFixture.IDEMPOTENCY_KEY)
                    .content("""
                        {
                            "propertyId": 1,
                            "roomTypeId": 1,
                            "ratePlanId": 1,
                            "checkIn": "2020-01-01",
                            "checkOut": "2020-01-03",
                            "guestCount": 2,
                            "totalAmount": 240000
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }

    // =========================================================================
    // POST /api/v1/reservations -- 예약 확정
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/reservations -- 예약 확정")
    class ConfirmReservation {

        @Test
        @DisplayName("유효한 요청 시 201 Created")
        void 정상_확정() throws Exception {
            given(confirmReservationUseCase.execute(any())).willReturn(42L);

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.confirmReservationRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(42))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("customer-confirm-reservation",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("sessionId").description("예약 세션 ID (필수)"),
                        fieldWithPath("customerId").description("고객 ID (필수)"),
                        fieldWithPath("guestInfo").description("투숙객 정보 (필수)"),
                        fieldWithPath("guestInfo.name").description("투숙객 이름 (필수)"),
                        fieldWithPath("guestInfo.phone").description("투숙객 전화번호 (선택)"),
                        fieldWithPath("guestInfo.email").description("투숙객 이메일 (선택)"),
                        fieldWithPath("bookingSnapshot").description("예약 스냅샷 (선택)"),
                        fieldWithPath("lines[]").description("예약 라인 목록 (1건 이상)"),
                        fieldWithPath("lines[].ratePlanId").description("요금제 ID (필수)"),
                        fieldWithPath("lines[].roomCount").description("객실 수 (필수)"),
                        fieldWithPath("lines[].subtotalAmount").description("소계 금액 (필수)"),
                        fieldWithPath("lines[].items[]").description("예약 항목 목록 (1건 이상)"),
                        fieldWithPath("lines[].items[].inventoryId").description("재고 ID (필수)"),
                        fieldWithPath("lines[].items[].stayDate").description("숙박 날짜 (필수)"),
                        fieldWithPath("lines[].items[].nightlyRate").description("1박 요금 (필수)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("생성된 예약 ID"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void 필수_필드_누락() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.confirmReservationInvalidRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("세션 없음 시 404 Not Found")
        void 세션_미존재() throws Exception {
            given(confirmReservationUseCase.execute(any()))
                .willThrow(new ReservationSessionNotFoundException());

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.confirmReservationRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RSV-005"))
                .andDo(document("customer-confirm-reservation-session-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }

        @Test
        @DisplayName("세션 만료 시 409 Conflict")
        void 세션_만료() throws Exception {
            given(confirmReservationUseCase.execute(any()))
                .willThrow(new ReservationSessionExpiredException());

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.confirmReservationRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RSV-006"))
                .andDo(document("customer-confirm-reservation-session-expired",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }

        @Test
        @DisplayName("재고 소진 시 409 Conflict")
        void 재고_부족() throws Exception {
            given(confirmReservationUseCase.execute(any()))
                .willThrow(new InventoryExhaustedException());

            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.confirmReservationRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INV-002"));
        }

        @Test
        @DisplayName("guestInfo.name이 빈 문자열이면 400 Bad Request (@NotBlank 위반)")
        void guestInfo_name_빈문자열_검증실패() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                            "sessionId": 1,
                            "customerId": 100,
                            "guestInfo": {
                                "name": "",
                                "phone": "010-1234-5678"
                            },
                            "lines": [
                                {
                                    "ratePlanId": 1,
                                    "roomCount": 1,
                                    "subtotalAmount": 120000,
                                    "items": [
                                        {
                                            "inventoryId": 1,
                                            "stayDate": "2026-06-01",
                                            "nightlyRate": 120000
                                        }
                                    ]
                                }
                            ]
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("lines가 빈 배열이면 400 Bad Request (@NotEmpty 위반)")
        void lines_빈배열_검증실패() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                            "sessionId": 1,
                            "customerId": 100,
                            "guestInfo": {
                                "name": "홍길동"
                            },
                            "lines": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("lines 내부 items가 빈 배열이면 400 Bad Request (@NotEmpty + @Valid 위반)")
        void lines_items_빈배열_검증실패() throws Exception {
            mockMvc.perform(post(CustomerReservationEndpoints.RESERVATIONS)
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                            "sessionId": 1,
                            "customerId": 100,
                            "guestInfo": {
                                "name": "홍길동"
                            },
                            "lines": [
                                {
                                    "ratePlanId": 1,
                                    "roomCount": 1,
                                    "subtotalAmount": 120000,
                                    "items": []
                                }
                            ]
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }

    // =========================================================================
    // PATCH /api/v1/reservations/{reservationId}/cancel -- 예약 취소
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/v1/reservations/{reservationId}/cancel -- 예약 취소")
    class CancelReservation {

        @Test
        @DisplayName("사유 포함 취소 시 200 OK")
        void 정상_취소_사유포함() throws Exception {
            willDoNothing().given(cancelReservationUseCase).execute(any());

            mockMvc.perform(patch(CustomerReservationEndpoints.RESERVATIONS + "/{reservationId}/cancel",
                        CustomerReservationFixture.RESERVATION_ID)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.cancelReservationRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("customer-cancel-reservation",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("reservationId").description("예약 ID")
                    ),
                    requestFields(
                        fieldWithPath("cancelReason").description("취소 사유 (선택)")
                    ),
                    responseFields(
                        fieldWithPath("data").description("응답 데이터 (void 시 null)"),
                        fieldWithPath("timestamp").description("응답 시각"),
                        fieldWithPath("requestId").description("요청 추적 ID")
                    )
                ));
        }

        @Test
        @DisplayName("사유 없이 취소 시 200 OK (body 없음)")
        void 정상_취소_사유없음() throws Exception {
            willDoNothing().given(cancelReservationUseCase).execute(any());

            mockMvc.perform(patch(CustomerReservationEndpoints.RESERVATIONS + "/{reservationId}/cancel",
                        CustomerReservationFixture.RESERVATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("존재하지 않는 예약 시 404 Not Found")
        void 예약_미존재() throws Exception {
            willThrow(new ReservationNotFoundException())
                .given(cancelReservationUseCase).execute(any());

            mockMvc.perform(patch(CustomerReservationEndpoints.RESERVATIONS + "/{reservationId}/cancel", 999L)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.cancelReservationRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RSV-001"))
                .andDo(document("customer-cancel-reservation-not-found",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }

        @Test
        @DisplayName("이미 취소된 예약 시 409 Conflict")
        void 이미_취소됨() throws Exception {
            willThrow(new ReservationAlreadyCancelledException())
                .given(cancelReservationUseCase).execute(any());

            mockMvc.perform(patch(CustomerReservationEndpoints.RESERVATIONS + "/{reservationId}/cancel",
                        CustomerReservationFixture.RESERVATION_ID)
                    .contentType(APPLICATION_JSON)
                    .content(CustomerReservationFixture.cancelReservationRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RSV-003"))
                .andDo(document("customer-cancel-reservation-already-cancelled",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                ));
        }
    }
}

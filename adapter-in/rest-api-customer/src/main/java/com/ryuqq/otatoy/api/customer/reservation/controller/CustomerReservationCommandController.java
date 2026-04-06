package com.ryuqq.otatoy.api.customer.reservation.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.customer.reservation.CustomerReservationEndpoints;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.CancelReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.CreateReservationSessionApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.response.ReservationSessionApiResponse;
import com.ryuqq.otatoy.api.customer.reservation.mapper.ReservationApiMapper;
import com.ryuqq.otatoy.application.reservation.port.in.CancelReservationUseCase;
import com.ryuqq.otatoy.application.reservation.port.in.ConfirmReservationUseCase;
import com.ryuqq.otatoy.application.reservation.port.in.CreateReservationSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 예약 Command Controller.
 * Command(쓰기) 전용 — POST/PATCH 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@Tag(name = "Customer - 예약", description = "고객용 예약 세션 생성, 예약 확정, 예약 취소 API")
public class CustomerReservationCommandController {

    private final CreateReservationSessionUseCase createReservationSessionUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    public CustomerReservationCommandController(
            CreateReservationSessionUseCase createReservationSessionUseCase,
            ConfirmReservationUseCase confirmReservationUseCase,
            CancelReservationUseCase cancelReservationUseCase) {
        this.createReservationSessionUseCase = createReservationSessionUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
    }

    @Operation(summary = "예약 세션 생성", description = "재고를 선점하고 예약 세션을 생성합니다 (1단계)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "세션 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 소진")
    })
    @PostMapping(CustomerReservationEndpoints.RESERVATION_SESSIONS)
    public ResponseEntity<ApiResponse<ReservationSessionApiResponse>> createSession(
            @Parameter(description = "멱등성 키", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateReservationSessionApiRequest request) {

        var command = ReservationApiMapper.toSessionCommand(idempotencyKey, request);
        var result = createReservationSessionUseCase.execute(command);
        var response = ReservationApiMapper.toSessionResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @Operation(summary = "예약 확정", description = "유효한 세션을 기반으로 예약을 확정합니다 (2단계)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "예약 확정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "세션 만료 또는 재고 부족")
    })
    @PostMapping(CustomerReservationEndpoints.RESERVATIONS)
    public ResponseEntity<ApiResponse<Long>> confirmReservation(
            @Valid @RequestBody ConfirmReservationApiRequest request) {

        var command = ReservationApiMapper.toConfirmCommand(request);
        Long reservationId = confirmReservationUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(reservationId));
    }

    @Operation(summary = "예약 취소", description = "예약을 취소합니다 (soft delete)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "예약 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 취소/완료된 예약")
    })
    @PatchMapping(CustomerReservationEndpoints.CANCEL)
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @Parameter(description = "예약 ID") @PathVariable(CustomerReservationEndpoints.PATH_RESERVATION_ID) Long reservationId,
            @RequestBody(required = false) CancelReservationApiRequest request) {

        String cancelReason = request != null ? request.cancelReason() : null;
        var command = ReservationApiMapper.toCancelCommand(reservationId, cancelReason);
        cancelReservationUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.of());
    }
}

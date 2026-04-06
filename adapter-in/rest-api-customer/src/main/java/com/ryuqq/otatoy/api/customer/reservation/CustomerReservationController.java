package com.ryuqq.otatoy.api.customer.reservation;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.customer.reservation.dto.CancelReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.ConfirmReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.CreateReservationSessionApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.mapper.ReservationApiMapper;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.application.reservation.port.in.ConfirmReservationUseCase;
import com.ryuqq.otatoy.application.reservation.port.in.CreateReservationSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 고객 예약 Controller.
 * UseCase 인터페이스만 의존하며, 비즈니스 로직 없이 HTTP 변환만 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@Tag(name = "Customer - 예약", description = "고객용 예약 세션 생성, 예약 확정, 예약 취소 API")
public class CustomerReservationController {

    private final CreateReservationSessionUseCase createReservationSessionUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    // TODO: CancelReservationUseCase 생성 후 주입 (현재 생성 중)
    // private final CancelReservationUseCase cancelReservationUseCase;

    public CustomerReservationController(
            CreateReservationSessionUseCase createReservationSessionUseCase,
            ConfirmReservationUseCase confirmReservationUseCase) {
        this.createReservationSessionUseCase = createReservationSessionUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
    }

    /**
     * 예약 세션 생성 (1단계: 재고 선점).
     * Idempotency-Key 헤더로 멱등성을 보장한다.
     */
    @Operation(summary = "예약 세션 생성", description = "재고를 선점하고 예약 세션을 생성합니다 (1단계)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "세션 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 소진")
    })
    @PostMapping(CustomerReservationEndpoints.RESERVATION_SESSIONS)
    public ResponseEntity<ApiResponse<ReservationSessionResult>> createSession(
            @Parameter(description = "멱등성 키", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateReservationSessionApiRequest request) {

        var command = ReservationApiMapper.toSessionCommand(idempotencyKey, request);
        var result = createReservationSessionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(result));
    }

    /**
     * 예약 확정 (2단계: 세션 → 예약 변환).
     * 유효한 세션을 기반으로 예약을 확정한다.
     */
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
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(reservationId));
    }

    /**
     * 예약 취소.
     * Soft delete 방식 — PATCH로 상태를 변경한다.
     * TODO: CancelReservationUseCase 생성 후 구현 완료 예정
     */
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

        // TODO: CancelReservationUseCase 생성 후 아래 주석 해제
        // String cancelReason = request != null ? request.cancelReason() : null;
        // cancelReservationUseCase.execute(reservationId, cancelReason);
        throw new UnsupportedOperationException("CancelReservationUseCase 미구현 — 구현 완료 후 활성화");
    }
}

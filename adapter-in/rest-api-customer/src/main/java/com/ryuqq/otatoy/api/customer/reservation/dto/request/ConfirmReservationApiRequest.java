package com.ryuqq.otatoy.api.customer.reservation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 예약 확정 요청 DTO.
 * 세션에 이미 숙소/객실/요금 정보가 있으므로,
 * 확정 시에는 세션ID + 고객/투숙객 정보 + 예약 라인만 필요하다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ConfirmReservationApiRequest(
    @NotNull(message = "세션 ID는 필수입니다")
    Long sessionId,

    @NotNull(message = "고객 ID는 필수입니다")
    Long customerId,

    @NotNull(message = "투숙객 정보는 필수입니다")
    @Valid
    GuestInfoApiRequest guestInfo,

    String bookingSnapshot,

    @NotEmpty(message = "예약 라인은 최소 1건 이상이어야 합니다")
    @Valid
    List<ReservationLineApiRequest> lines
) {

    /**
     * 투숙객 정보 요청 DTO.
     */
    public record GuestInfoApiRequest(
        @NotBlank(message = "투숙객 이름은 필수입니다")
        String name,

        String phone,

        String email
    ) {}

    /**
     * 예약 라인 요청 DTO.
     */
    public record ReservationLineApiRequest(
        @NotNull(message = "요금제 ID는 필수입니다")
        Long ratePlanId,

        @NotNull(message = "객실 수는 필수입니다")
        int roomCount,

        @NotNull(message = "소계 금액은 필수입니다")
        java.math.BigDecimal subtotalAmount,

        @NotEmpty(message = "예약 항목은 최소 1건 이상이어야 합니다")
        @Valid
        List<ReservationItemApiRequest> items
    ) {}

    /**
     * 예약 항목(날짜별) 요청 DTO.
     */
    public record ReservationItemApiRequest(
        @NotNull(message = "재고 ID는 필수입니다")
        Long inventoryId,

        @NotNull(message = "숙박 날짜는 필수입니다")
        java.time.LocalDate stayDate,

        @NotNull(message = "1박 요금은 필수입니다")
        java.math.BigDecimal nightlyRate
    ) {}
}

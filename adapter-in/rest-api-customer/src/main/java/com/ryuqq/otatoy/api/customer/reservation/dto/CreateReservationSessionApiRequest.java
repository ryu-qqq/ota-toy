package com.ryuqq.otatoy.api.customer.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 예약 세션 생성 요청 DTO.
 * 원시 타입만 사용하며, Domain VO 변환은 ReservationApiMapper가 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateReservationSessionApiRequest(
    @NotNull(message = "숙소 ID는 필수입니다")
    Long propertyId,

    @NotNull(message = "객실 유형 ID는 필수입니다")
    Long roomTypeId,

    @NotNull(message = "요금제 ID는 필수입니다")
    Long ratePlanId,

    @NotNull(message = "체크인 날짜는 필수입니다")
    @Future(message = "체크인 날짜는 미래여야 합니다")
    LocalDate checkIn,

    @NotNull(message = "체크아웃 날짜는 필수입니다")
    @Future(message = "체크아웃 날짜는 미래여야 합니다")
    LocalDate checkOut,

    @Min(value = 1, message = "투숙 인원은 1명 이상이어야 합니다")
    int guestCount,

    @NotNull(message = "총 금액은 필수입니다")
    BigDecimal totalAmount
) {}

package com.ryuqq.otatoy.api.customer.reservation.dto.request;

/**
 * 예약 취소 요청 DTO.
 * cancelReason은 선택 필드이다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CancelReservationApiRequest(
    String cancelReason
) {}

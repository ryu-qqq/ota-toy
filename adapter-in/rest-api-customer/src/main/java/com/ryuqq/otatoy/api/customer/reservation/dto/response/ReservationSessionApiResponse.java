package com.ryuqq.otatoy.api.customer.reservation.dto.response;

import java.math.BigDecimal;

/**
 * 예약 세션 생성 API 응답 DTO.
 * 도메인 VO(Money, Instant)를 원시 타입으로 변환하여 JSON 직렬화 문제를 방지한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record ReservationSessionApiResponse(
    Long sessionId,
    BigDecimal totalAmount,
    String checkIn,
    String checkOut,
    int guestCount,
    String expiresAt
) {}

package com.ryuqq.otatoy.api.customer.rate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 요금 조회 요청 DTO.
 * GET /api/v1/properties/{propertyId}/rates 의 쿼리 파라미터를 바인딩한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record FetchRateApiRequest(
        @NotNull(message = "체크인 날짜는 필수입니다")
        LocalDate checkIn,

        @NotNull(message = "체크아웃 날짜는 필수입니다")
        LocalDate checkOut,

        @Min(value = 1, message = "투숙 인원은 1명 이상이어야 합니다")
        int guests
) {

    /**
     * 기본값을 적용한 생성자.
     */
    public FetchRateApiRequest {
        if (guests <= 0) {
            guests = 1;
        }
    }
}

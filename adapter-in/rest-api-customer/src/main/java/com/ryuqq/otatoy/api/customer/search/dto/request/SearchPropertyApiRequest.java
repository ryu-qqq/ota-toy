package com.ryuqq.otatoy.api.customer.search.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 숙소 검색 요청 DTO.
 * GET /api/v1/search/properties 의 쿼리 파라미터를 바인딩한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SearchPropertyApiRequest(
        String keyword,
        String region,
        Long propertyTypeId,

        @NotNull(message = "체크인 날짜는 필수입니다")
        LocalDate checkIn,

        @NotNull(message = "체크아웃 날짜는 필수입니다")
        LocalDate checkOut,

        @Min(value = 1, message = "투숙 인원은 1명 이상이어야 합니다")
        int guests,

        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> amenityTypes,
        Boolean freeCancellationOnly,
        Integer starRating,
        String sortKey,
        String direction,

        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        int size,

        Long cursor
) {

    /**
     * 기본값을 적용한 생성자.
     */
    public SearchPropertyApiRequest {
        if (size <= 0) {
            size = 20;
        }
        if (guests <= 0) {
            guests = 1;
        }
    }
}

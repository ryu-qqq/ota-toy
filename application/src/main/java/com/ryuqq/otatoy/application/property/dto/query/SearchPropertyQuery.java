package com.ryuqq.otatoy.application.property.dto.query;

import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * 고객 숙소 검색 UseCase 입력 DTO.
 * Controller(ApiMapper)에서 변환하여 전달한다 (APP-DTO-001).
 * 인스턴스 메서드 금지 -- 순수 데이터 컨테이너 역할만 한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SearchPropertyQuery(
        String keyword,
        String region,
        PropertyTypeId propertyTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        Money minPrice,
        Money maxPrice,
        List<AmenityType> amenityTypes,
        boolean freeCancellationOnly,
        Integer starRating,
        PropertySortKey sortKey,
        SortDirection direction,
        int size,
        Long cursor
) {
}

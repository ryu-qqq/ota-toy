package com.ryuqq.otatoy.api.customer.search.mapper;

import com.ryuqq.otatoy.api.customer.search.dto.PropertySummaryApiResponse;
import com.ryuqq.otatoy.api.customer.search.dto.SearchPropertyApiRequest;
import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.util.List;

/**
 * 숙소 검색 API 변환 매퍼.
 * Request → Query, Result → Response 변환을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class SearchPropertyApiMapper {

    private SearchPropertyApiMapper() {
    }

    public static CustomerSearchPropertyQuery toQuery(SearchPropertyApiRequest request) {
        return new CustomerSearchPropertyQuery(
                request.keyword(),
                request.region(),
                request.propertyTypeId() != null ? PropertyTypeId.of(request.propertyTypeId()) : null,
                request.checkIn(),
                request.checkOut(),
                request.guests(),
                request.minPrice() != null ? Money.of(request.minPrice()) : null,
                request.maxPrice() != null ? Money.of(request.maxPrice()) : null,
                parseAmenityTypes(request.amenityTypes()),
                request.freeCancellationOnly() != null && request.freeCancellationOnly(),
                request.starRating(),
                parseSortKey(request.sortKey()),
                parseDirection(request.direction()),
                request.size(),
                request.cursor()
        );
    }

    public static PropertySummaryApiResponse toApiResponse(PropertySummary summary) {
        return new PropertySummaryApiResponse(
                summary.propertyId().value(),
                summary.name().value(),
                summary.propertyTypeId().value(),
                summary.location().address(),
                summary.location().latitude(),
                summary.location().longitude(),
                summary.location().region(),
                summary.lowestPrice() != null ? summary.lowestPrice().amount() : null
        );
    }

    private static List<AmenityType> parseAmenityTypes(List<String> amenityTypes) {
        if (amenityTypes == null || amenityTypes.isEmpty()) {
            return List.of();
        }
        return amenityTypes.stream()
                .map(AmenityType::valueOf)
                .toList();
    }

    private static PropertySortKey parseSortKey(String sortKey) {
        if (sortKey == null || sortKey.isBlank()) {
            return null;
        }
        return PropertySortKey.valueOf(sortKey);
    }

    private static SortDirection parseDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }
        return SortDirection.valueOf(direction);
    }
}

package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

import org.springframework.stereotype.Component;

/**
 * Query DTO → Domain Criteria 변환 Factory.
 * Service에서 직접 new하지 않고 Factory에서 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertySearchCriteriaFactory {

    public ExtranetPropertySliceCriteria createForExtranet(ExtranetSearchPropertyQuery query) {
        return new ExtranetPropertySliceCriteria(
                query.partnerId(),
                query.size(),
                query.cursor()
        );
    }

    public PropertySliceCriteria create(CustomerSearchPropertyQuery query) {
        return new PropertySliceCriteria(
                query.keyword(),
                query.region(),
                query.propertyTypeId(),
                query.checkIn(),
                query.checkOut(),
                query.guests(),
                query.minPrice(),
                query.maxPrice(),
                query.amenityTypes(),
                query.freeCancellationOnly(),
                query.starRating(),
                query.sortKey(),
                query.direction(),
                query.size(),
                query.cursor()
        );
    }
}

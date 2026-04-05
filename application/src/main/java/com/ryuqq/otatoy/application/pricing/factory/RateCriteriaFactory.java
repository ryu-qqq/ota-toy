package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;

import org.springframework.stereotype.Component;

/**
 * Query DTO → Domain Criteria 변환 Factory.
 * Service에서 직접 new하지 않고 Factory에서 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateCriteriaFactory {

    public RateFetchCriteria create(CustomerGetRateQuery query) {
        return new RateFetchCriteria(
                query.propertyId(),
                query.checkIn(),
                query.checkOut(),
                query.guests()
        );
    }
}

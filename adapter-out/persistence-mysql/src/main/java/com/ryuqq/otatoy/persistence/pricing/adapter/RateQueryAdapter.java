package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RateQueryPort;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateQueryDslRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Rate Query Adapter.
 * RateQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateQueryAdapter implements RateQueryPort {

    private final RateQueryDslRepository queryDslRepository;
    private final RateEntityMapper mapper;

    public RateQueryAdapter(RateQueryDslRepository queryDslRepository, RateEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Rate> findByRatePlanIdsAndDateRange(List<RatePlanId> ratePlanIds,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        List<Long> ids = ratePlanIds.stream()
                .map(RatePlanId::value)
                .toList();
        return queryDslRepository.findByRatePlanIdsAndDateRange(ids, startDate, endDate).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

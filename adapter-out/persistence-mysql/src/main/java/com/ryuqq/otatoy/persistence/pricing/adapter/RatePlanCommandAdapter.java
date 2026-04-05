package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanCommandPort;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.persistence.pricing.entity.RatePlanJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanJpaRepository;
import org.springframework.stereotype.Component;

/**
 * RatePlan Command Adapter.
 * RatePlanCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanCommandAdapter implements RatePlanCommandPort {

    private final RatePlanJpaRepository jpaRepository;
    private final RatePlanEntityMapper mapper;

    public RatePlanCommandAdapter(RatePlanJpaRepository jpaRepository, RatePlanEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(RatePlan ratePlan) {
        RatePlanJpaEntity entity = mapper.toEntity(ratePlan);
        RatePlanJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

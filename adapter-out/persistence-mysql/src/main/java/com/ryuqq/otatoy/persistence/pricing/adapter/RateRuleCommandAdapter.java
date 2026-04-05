package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RateRuleCommandPort;
import com.ryuqq.otatoy.domain.pricing.RateRule;
import com.ryuqq.otatoy.persistence.pricing.entity.RateRuleJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateRuleEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateRuleJpaRepository;
import org.springframework.stereotype.Component;

/**
 * RateRule Command Adapter.
 * RateRuleCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateRuleCommandAdapter implements RateRuleCommandPort {

    private final RateRuleJpaRepository jpaRepository;
    private final RateRuleEntityMapper mapper;

    public RateRuleCommandAdapter(RateRuleJpaRepository jpaRepository, RateRuleEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(RateRule rateRule) {
        RateRuleJpaEntity entity = mapper.toEntity(rateRule);
        RateRuleJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

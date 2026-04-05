package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RateOverrideCommandPort;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.persistence.pricing.entity.RateOverrideJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateOverrideEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateOverrideJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RateOverride Command Adapter.
 * RateOverrideCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateOverrideCommandAdapter implements RateOverrideCommandPort {

    private final RateOverrideJpaRepository jpaRepository;
    private final RateOverrideEntityMapper mapper;

    public RateOverrideCommandAdapter(RateOverrideJpaRepository jpaRepository, RateOverrideEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persistAll(List<RateOverride> overrides) {
        List<RateOverrideJpaEntity> entities = overrides.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

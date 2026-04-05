package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RateCommandPort;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.persistence.pricing.entity.RateJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rate Command Adapter.
 * RateCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateCommandAdapter implements RateCommandPort {

    private final RateJpaRepository jpaRepository;
    private final RateEntityMapper mapper;

    public RateCommandAdapter(RateJpaRepository jpaRepository, RateEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persistAll(List<Rate> rates) {
        List<RateJpaEntity> entities = rates.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

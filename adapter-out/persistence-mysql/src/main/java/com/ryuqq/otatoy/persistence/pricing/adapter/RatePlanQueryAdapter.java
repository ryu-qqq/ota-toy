package com.ryuqq.otatoy.persistence.pricing.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanQueryPort;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * RatePlan Query Adapter.
 * RatePlanQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanQueryAdapter implements RatePlanQueryPort {

    private final RatePlanQueryDslRepository queryDslRepository;
    private final RatePlanEntityMapper mapper;

    public RatePlanQueryAdapter(RatePlanQueryDslRepository queryDslRepository, RatePlanEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RatePlan> findById(RatePlanId id) {
        return queryDslRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(RatePlanId id) {
        return queryDslRepository.existsById(id.value());
    }

    @Override
    public List<RatePlan> findByRoomTypeIds(List<RoomTypeId> roomTypeIds) {
        List<Long> ids = roomTypeIds.stream()
                .map(RoomTypeId::value)
                .toList();
        return queryDslRepository.findByRoomTypeIds(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

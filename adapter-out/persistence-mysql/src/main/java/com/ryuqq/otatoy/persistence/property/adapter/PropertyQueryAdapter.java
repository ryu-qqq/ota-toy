package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Property Query Adapter.
 * PropertyQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyQueryAdapter implements PropertyQueryPort {

    private final PropertyQueryDslRepository queryDslRepository;
    private final PropertyEntityMapper mapper;

    public PropertyQueryAdapter(PropertyQueryDslRepository queryDslRepository, PropertyEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return queryDslRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(PropertyId id) {
        return queryDslRepository.existsById(id.value());
    }

    @Override
    public SliceResult<Property> findByCondition(PropertySliceCriteria criteria) {
        // TODO: STORY-104 후속 구현 — QueryDslRepository에 조건 조회 추가 필요
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }
}

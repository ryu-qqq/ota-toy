package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public SliceResult<Property> findByCriteria(ExtranetPropertySliceCriteria criteria) {
        List<PropertyJpaEntity> entities = queryDslRepository.findByPartnerId(
                criteria.partnerId().value(), criteria.size(), criteria.cursor());

        boolean hasNext = entities.size() > criteria.size();
        List<Property> content = entities.stream()
                .limit(criteria.size())
                .map(mapper::toDomain)
                .toList();

        Long nextCursor = hasNext ? content.get(content.size() - 1).id().value() : null;
        return SliceResult.of(content, new SliceMeta(hasNext, nextCursor));
    }
}

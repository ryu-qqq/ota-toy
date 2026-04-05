package com.ryuqq.otatoy.persistence.propertytype.adapter;

import com.ryuqq.otatoy.application.propertytype.port.out.PropertyTypeQueryPort;
import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.persistence.propertytype.mapper.PropertyTypeEntityMapper;
import com.ryuqq.otatoy.persistence.propertytype.repository.PropertyTypeQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * PropertyType Query Adapter.
 * PropertyTypeQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyTypeQueryAdapter implements PropertyTypeQueryPort {

    private final PropertyTypeQueryDslRepository queryDslRepository;
    private final PropertyTypeEntityMapper mapper;

    public PropertyTypeQueryAdapter(PropertyTypeQueryDslRepository queryDslRepository,
                                     PropertyTypeEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PropertyType> findById(PropertyTypeId id) {
        return queryDslRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(PropertyTypeId id) {
        return queryDslRepository.existsById(id.value());
    }

    @Override
    public List<PropertyTypeAttribute> findAttributesByPropertyTypeId(PropertyTypeId propertyTypeId) {
        return queryDslRepository.findAttributesByPropertyTypeId(propertyTypeId.value()).stream()
                .map(mapper::toAttributeDomain)
                .toList();
    }
}

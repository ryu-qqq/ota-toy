package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAttributeValueEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAttributeValueQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PropertyAttributeValue Query Adapter.
 * PropertyAttributeValueQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueQueryAdapter implements PropertyAttributeValueQueryPort {

    private final PropertyAttributeValueQueryDslRepository queryDslRepository;
    private final PropertyAttributeValueEntityMapper mapper;

    public PropertyAttributeValueQueryAdapter(PropertyAttributeValueQueryDslRepository queryDslRepository,
                                               PropertyAttributeValueEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public PropertyAttributeValues findByPropertyId(PropertyId propertyId) {
        List<PropertyAttributeValue> items = queryDslRepository.findByPropertyId(propertyId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
        return PropertyAttributeValues.reconstitute(items);
    }
}

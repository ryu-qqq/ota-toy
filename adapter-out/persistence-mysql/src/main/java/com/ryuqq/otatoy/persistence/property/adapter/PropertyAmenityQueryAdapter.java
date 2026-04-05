package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAmenityEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAmenityQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PropertyAmenity Query Adapter.
 * PropertyAmenityQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityQueryAdapter implements PropertyAmenityQueryPort {

    private final PropertyAmenityQueryDslRepository queryDslRepository;
    private final PropertyAmenityEntityMapper mapper;

    public PropertyAmenityQueryAdapter(PropertyAmenityQueryDslRepository queryDslRepository,
                                        PropertyAmenityEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public PropertyAmenities findByPropertyId(PropertyId propertyId) {
        List<PropertyAmenity> items = queryDslRepository.findByPropertyId(propertyId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
        return PropertyAmenities.reconstitute(items);
    }
}

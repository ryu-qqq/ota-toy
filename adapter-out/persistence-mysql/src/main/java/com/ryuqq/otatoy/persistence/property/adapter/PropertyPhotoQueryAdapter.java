package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyPhotoEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyPhotoQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PropertyPhoto Query Adapter.
 * PropertyPhotoQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyPhotoQueryAdapter implements PropertyPhotoQueryPort {

    private final PropertyPhotoQueryDslRepository queryDslRepository;
    private final PropertyPhotoEntityMapper mapper;

    public PropertyPhotoQueryAdapter(PropertyPhotoQueryDslRepository queryDslRepository,
                                      PropertyPhotoEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<PropertyPhoto> findByPropertyId(PropertyId propertyId) {
        return queryDslRepository.findByPropertyId(propertyId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

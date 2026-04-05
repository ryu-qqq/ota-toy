package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.persistence.property.entity.PropertyPhotoJpaEntity;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyPhotoEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyPhotoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PropertyPhoto Command Adapter.
 * PropertyPhotoCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyPhotoCommandAdapter implements PropertyPhotoCommandPort {

    private final PropertyPhotoJpaRepository jpaRepository;
    private final PropertyPhotoEntityMapper mapper;

    public PropertyPhotoCommandAdapter(PropertyPhotoJpaRepository jpaRepository, PropertyPhotoEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public int persistAll(List<PropertyPhoto> photos) {
        List<PropertyPhotoJpaEntity> entities = photos.stream()
                .map(mapper::toEntity)
                .toList();
        List<PropertyPhotoJpaEntity> saved = jpaRepository.saveAll(entities);
        return saved.size();
    }
}

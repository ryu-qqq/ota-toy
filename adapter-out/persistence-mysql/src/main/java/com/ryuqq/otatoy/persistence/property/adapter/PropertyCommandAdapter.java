package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyCommandPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Property Command Adapter.
 * PropertyCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyCommandAdapter implements PropertyCommandPort {

    private final PropertyJpaRepository jpaRepository;
    private final PropertyEntityMapper mapper;

    public PropertyCommandAdapter(PropertyJpaRepository jpaRepository, PropertyEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(Property property) {
        PropertyJpaEntity entity = mapper.toEntity(property);
        PropertyJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }

    @Override
    public void persistAll(List<Property> properties) {
        List<PropertyJpaEntity> entities = properties.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

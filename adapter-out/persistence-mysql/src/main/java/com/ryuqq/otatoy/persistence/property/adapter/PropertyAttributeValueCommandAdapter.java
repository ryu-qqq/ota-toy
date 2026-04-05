package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAttributeValueJpaEntity;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAttributeValueEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAttributeValueJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PropertyAttributeValue Command Adapter.
 * PropertyAttributeValueCommandPort를 구현한다.
 * diff 패턴 적용으로 JpaRepository만 의존한다 (QueryDslRepository 의존 제거).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueCommandAdapter implements PropertyAttributeValueCommandPort {

    private final PropertyAttributeValueJpaRepository jpaRepository;
    private final PropertyAttributeValueEntityMapper mapper;

    public PropertyAttributeValueCommandAdapter(PropertyAttributeValueJpaRepository jpaRepository,
                                                  PropertyAttributeValueEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void persistAll(List<PropertyAttributeValue> attributeValues) {
        if (attributeValues != null && !attributeValues.isEmpty()) {
            List<PropertyAttributeValueJpaEntity> entities = attributeValues.stream()
                    .map(mapper::toEntity)
                    .toList();
            jpaRepository.saveAll(entities);
        }
    }
}

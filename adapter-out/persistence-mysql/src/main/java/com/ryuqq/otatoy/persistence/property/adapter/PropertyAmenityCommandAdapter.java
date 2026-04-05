package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAmenityJpaEntity;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAmenityEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAmenityJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PropertyAmenity Command Adapter.
 * PropertyAmenityCommandPort를 구현한다.
 * diff 패턴 적용으로 JpaRepository만 의존한다 (QueryDslRepository 의존 제거).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityCommandAdapter implements PropertyAmenityCommandPort {

    private final PropertyAmenityJpaRepository jpaRepository;
    private final PropertyAmenityEntityMapper mapper;

    public PropertyAmenityCommandAdapter(PropertyAmenityJpaRepository jpaRepository,
                                          PropertyAmenityEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void persistAll(List<PropertyAmenity> amenities) {
        if (amenities != null && !amenities.isEmpty()) {
            List<PropertyAmenityJpaEntity> entities = amenities.stream()
                    .map(mapper::toEntity)
                    .toList();
            jpaRepository.saveAll(entities);
        }
    }
}

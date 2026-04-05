package com.ryuqq.otatoy.persistence.property.repository;

import com.ryuqq.otatoy.persistence.property.entity.PropertyAttributeValueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PropertyAttributeValue Command 전용 JPA Repository.
 * save/saveAll만 사용. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAttributeValueJpaRepository extends JpaRepository<PropertyAttributeValueJpaEntity, Long> {
}

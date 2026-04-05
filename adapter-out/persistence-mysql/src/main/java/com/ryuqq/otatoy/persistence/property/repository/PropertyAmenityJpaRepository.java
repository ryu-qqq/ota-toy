package com.ryuqq.otatoy.persistence.property.repository;

import com.ryuqq.otatoy.persistence.property.entity.PropertyAmenityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PropertyAmenity Command 전용 JPA Repository.
 * save/saveAll만 사용. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAmenityJpaRepository extends JpaRepository<PropertyAmenityJpaEntity, Long> {
}

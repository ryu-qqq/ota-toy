package com.ryuqq.otatoy.persistence.roomtype.repository;

import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeBedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RoomTypeBed Command 전용 JpaRepository.
 * save/saveAll만 사용한다. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface RoomTypeBedJpaRepository extends JpaRepository<RoomTypeBedJpaEntity, Long> {
    // 커스텀 메서드 추가 금지 (PER-REP-001)
}

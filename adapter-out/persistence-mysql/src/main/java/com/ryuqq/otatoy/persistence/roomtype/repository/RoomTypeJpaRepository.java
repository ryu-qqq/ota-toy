package com.ryuqq.otatoy.persistence.roomtype.repository;

import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RoomType Command 전용 JpaRepository.
 * save/saveAll만 사용한다. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface RoomTypeJpaRepository extends JpaRepository<RoomTypeJpaEntity, Long> {
    // 커스텀 메서드 추가 금지 (PER-REP-001)
}

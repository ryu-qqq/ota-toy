package com.ryuqq.otatoy.persistence.reservation.repository;

import com.ryuqq.otatoy.persistence.reservation.entity.ReservationSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReservationSession Command 전용 JPA Repository.
 * save/saveAll만 사용. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationSessionJpaRepository extends JpaRepository<ReservationSessionJpaEntity, Long> {
}

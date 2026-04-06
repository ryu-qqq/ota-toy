package com.ryuqq.otatoy.persistence.reservation.repository;

import com.ryuqq.otatoy.persistence.reservation.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Reservation Command 전용 JPA Repository.
 * save/saveAll만 사용. 커스텀 메서드 추가 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {
}

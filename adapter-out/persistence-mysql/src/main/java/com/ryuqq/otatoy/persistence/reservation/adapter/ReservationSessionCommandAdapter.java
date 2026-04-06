package com.ryuqq.otatoy.persistence.reservation.adapter;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionCommandPort;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationSessionJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationSessionEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationSessionJpaRepository;
import org.springframework.stereotype.Component;

/**
 * ReservationSession Command Adapter.
 * ReservationSessionCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionCommandAdapter implements ReservationSessionCommandPort {

    private final ReservationSessionJpaRepository jpaRepository;
    private final ReservationSessionEntityMapper mapper;

    public ReservationSessionCommandAdapter(ReservationSessionJpaRepository jpaRepository,
                                             ReservationSessionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(ReservationSession session) {
        ReservationSessionJpaEntity entity = mapper.toEntity(session);
        ReservationSessionJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

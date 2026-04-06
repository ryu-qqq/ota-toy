package com.ryuqq.otatoy.persistence.reservation.adapter;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionQueryPort;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationSessionEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationSessionQueryDslRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ReservationSession Query Adapter.
 * ReservationSessionQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionQueryAdapter implements ReservationSessionQueryPort {

    private final ReservationSessionQueryDslRepository queryDslRepository;
    private final ReservationSessionEntityMapper mapper;

    public ReservationSessionQueryAdapter(ReservationSessionQueryDslRepository queryDslRepository,
                                           ReservationSessionEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ReservationSession> findById(Long id) {
        return queryDslRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ReservationSession> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    public List<ReservationSession> findPendingBefore(Instant cutoff) {
        return queryDslRepository.findPendingBefore(cutoff).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

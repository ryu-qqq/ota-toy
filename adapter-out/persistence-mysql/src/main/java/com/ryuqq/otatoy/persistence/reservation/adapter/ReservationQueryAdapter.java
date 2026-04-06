package com.ryuqq.otatoy.persistence.reservation.adapter;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationQueryPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReservationQueryAdapter implements ReservationQueryPort {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationEntityMapper mapper;

    public ReservationQueryAdapter(ReservationJpaRepository reservationJpaRepository,
                                    ReservationEntityMapper mapper) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return reservationJpaRepository.findById(id.value())
            .map(entity -> mapper.toDomain(entity, List.of()));
    }
}

package com.ryuqq.otatoy.persistence.reservation.adapter;

import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationItem;
import com.ryuqq.otatoy.domain.reservation.ReservationLine;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationItemJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationLineJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationItemJpaRepository;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationJpaRepository;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationLineJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Reservation Command Adapter.
 * ReservationCommandPort를 구현하며, JpaRepository만 의존한다.
 * Reservation + ReservationLine + ReservationItem을 원자적으로 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationCommandAdapter implements ReservationCommandPort {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationLineJpaRepository lineJpaRepository;
    private final ReservationItemJpaRepository itemJpaRepository;
    private final ReservationEntityMapper mapper;

    public ReservationCommandAdapter(ReservationJpaRepository reservationJpaRepository,
                                      ReservationLineJpaRepository lineJpaRepository,
                                      ReservationItemJpaRepository itemJpaRepository,
                                      ReservationEntityMapper mapper) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.lineJpaRepository = lineJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(Reservation reservation) {
        // 1. Reservation 저장
        ReservationJpaEntity reservationEntity = mapper.toEntity(reservation);
        ReservationJpaEntity savedReservation = reservationJpaRepository.save(reservationEntity);
        Long reservationId = savedReservation.getId();

        // 2. ReservationLine 저장
        List<ReservationItemJpaEntity> allItemEntities = new ArrayList<>();
        for (ReservationLine line : reservation.lines()) {
            ReservationLineJpaEntity lineEntity = mapper.toLineEntity(line, reservationId);
            ReservationLineJpaEntity savedLine = lineJpaRepository.save(lineEntity);
            Long lineId = savedLine.getId();

            // 3. ReservationItem 저장
            for (ReservationItem item : line.items()) {
                ReservationItemJpaEntity itemEntity = mapper.toItemEntity(item, reservationId, lineId);
                allItemEntities.add(itemEntity);
            }
        }

        if (!allItemEntities.isEmpty()) {
            itemJpaRepository.saveAll(allItemEntities);
        }

        return reservationId;
    }
}

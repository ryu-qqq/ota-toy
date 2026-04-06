package com.ryuqq.otatoy.application.reservation.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import org.springframework.stereotype.Component;

/**
 * ReservationSession 도메인 객체 생성 Factory (APP-FAC-001).
 * TimeProvider를 주입받아 시간을 제공한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionFactory {

    private final TimeProvider timeProvider;

    public ReservationSessionFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * CreateReservationSessionCommand로부터 ReservationSession을 생성한다.
     */
    public ReservationSession create(CreateReservationSessionCommand command) {
        return ReservationSession.forNew(
            command.idempotencyKey(),
            command.propertyId(),
            command.roomTypeId(),
            command.ratePlanId(),
            command.checkIn(),
            command.checkOut(),
            command.guestCount(),
            command.totalAmount(),
            timeProvider.now()
        );
    }
}

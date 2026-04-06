package com.ryuqq.otatoy.application.reservation.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationItemCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationLineCommand;
import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationItem;
import com.ryuqq.otatoy.domain.reservation.ReservationLine;
import com.ryuqq.otatoy.domain.reservation.ReservationNo;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Reservation 도메인 객체 생성 Factory (APP-FAC-001).
 * TimeProvider를 주입받아 시간을 제공한다.
 * Instant.now() / LocalDateTime.now() 직접 호출 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationFactory {

    private final TimeProvider timeProvider;

    public ReservationFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * CreateReservationCommand로부터 Reservation Aggregate를 생성한다.
     * ReservationNo는 UUID 기반으로 생성하며, 상태는 PENDING으로 시작한다.
     */
    public Reservation create(CreateReservationCommand command) {
        Instant now = timeProvider.now();
        LocalDate today = timeProvider.today();

        ReservationNo reservationNo = ReservationNo.of(generateReservationNo());
        DateRange stayPeriod = new DateRange(command.checkIn(), command.checkOut());

        List<ReservationLine> lines = command.lines().stream()
            .map(lineCommand -> createLine(lineCommand, now))
            .toList();

        return Reservation.forNew(
            command.customerId(),
            reservationNo,
            command.guestInfo(),
            stayPeriod,
            command.guestCount(),
            command.totalAmount(),
            command.bookingSnapshot(),
            lines,
            today,
            now
        );
    }

    private ReservationLine createLine(CreateReservationLineCommand lineCommand, Instant now) {
        List<ReservationItem> items = lineCommand.items().stream()
            .map(itemCommand -> createItem(itemCommand, now))
            .toList();

        return ReservationLine.forNew(
            null,
            lineCommand.ratePlanId(),
            lineCommand.roomCount(),
            lineCommand.subtotalAmount(),
            items,
            now
        );
    }

    private ReservationItem createItem(CreateReservationItemCommand itemCommand, Instant now) {
        return ReservationItem.forNew(
            itemCommand.inventoryId(),
            itemCommand.stayDate(),
            itemCommand.nightlyRate(),
            now
        );
    }

    private String generateReservationNo() {
        return "RSV-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}

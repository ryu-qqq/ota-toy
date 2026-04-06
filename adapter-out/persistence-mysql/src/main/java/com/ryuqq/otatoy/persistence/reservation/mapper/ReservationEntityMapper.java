package com.ryuqq.otatoy.persistence.reservation.mapper;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.reservation.ReservationItem;
import com.ryuqq.otatoy.domain.reservation.ReservationItemId;
import com.ryuqq.otatoy.domain.reservation.ReservationLine;
import com.ryuqq.otatoy.domain.reservation.ReservationLineId;
import com.ryuqq.otatoy.domain.reservation.ReservationNo;
import com.ryuqq.otatoy.domain.reservation.ReservationStatus;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationItemJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationLineJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reservation Domain <-> Entity 변환 전담 Mapper.
 * Reservation, ReservationLine, ReservationItem 모두 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationEntityMapper {

    /**
     * Reservation Domain -> Entity 변환.
     */
    public ReservationJpaEntity toEntity(Reservation domain) {
        return ReservationJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.customerId(),
                domain.reservationNo().value(),
                domain.guestInfo().name(),
                domain.guestInfo().phoneValue(),
                domain.guestInfo().emailValue(),
                domain.stayPeriod().startDate(),
                domain.stayPeriod().endDate(),
                domain.guestCount(),
                domain.totalAmount().amount(),
                domain.status().name(),
                domain.cancelReason(),
                domain.bookingSnapshot(),
                domain.cancelledAt(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * ReservationLine Domain -> Entity 변환.
     * reservationId는 Reservation 저장 후 획득한 ID를 사용한다.
     */
    public ReservationLineJpaEntity toLineEntity(ReservationLine domain, Long reservationId) {
        return ReservationLineJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                reservationId,
                domain.ratePlanId().value(),
                domain.roomCount(),
                domain.subtotalAmount().amount(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * ReservationItem Domain -> Entity 변환.
     * reservationId, reservationLineId는 상위 엔티티 저장 후 획득한 ID를 사용한다.
     */
    public ReservationItemJpaEntity toItemEntity(ReservationItem domain, Long reservationId, Long reservationLineId) {
        return ReservationItemJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                reservationId,
                reservationLineId,
                domain.inventoryId().value(),
                domain.stayDate(),
                domain.nightlyRate().amount(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     * Line과 Item은 별도 조회하여 조립한다.
     */
    public Reservation toDomain(ReservationJpaEntity entity, List<ReservationLine> lines) {
        return Reservation.reconstitute(
                ReservationId.of(entity.getId()),
                entity.getCustomerId(),
                ReservationNo.of(entity.getReservationNo()),
                GuestInfo.of(entity.getGuestName(), entity.getGuestPhone(), entity.getGuestEmail()),
                new DateRange(entity.getCheckInDate(), entity.getCheckOutDate()),
                entity.getGuestCount(),
                Money.of(entity.getTotalAmount()),
                ReservationStatus.valueOf(entity.getStatus()),
                entity.getCancelReason(),
                entity.getBookingSnapshot(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCancelledAt(),
                lines
        );
    }

    /**
     * ReservationLine Entity -> Domain 변환.
     */
    public ReservationLine toLineDomain(ReservationLineJpaEntity entity, List<ReservationItem> items) {
        return ReservationLine.reconstitute(
                ReservationLineId.of(entity.getId()),
                ReservationId.of(entity.getReservationId()),
                RatePlanId.of(entity.getRatePlanId()),
                entity.getRoomCount(),
                Money.of(entity.getSubtotalAmount()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }

    /**
     * ReservationItem Entity -> Domain 변환.
     */
    public ReservationItem toItemDomain(ReservationItemJpaEntity entity) {
        return ReservationItem.reconstitute(
                ReservationItemId.of(entity.getId()),
                InventoryId.of(entity.getInventoryId()),
                entity.getStayDate(),
                Money.of(entity.getNightlyRate()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

package com.ryuqq.otatoy.persistence.reservation.mapper;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionId;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionStatus;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationSessionJpaEntity;
import org.springframework.stereotype.Component;

/**
 * ReservationSession Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public ReservationSessionJpaEntity toEntity(ReservationSession domain) {
        return ReservationSessionJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.idempotencyKey(),
                domain.propertyId().value(),
                domain.roomTypeId().value(),
                domain.ratePlanId().value(),
                domain.checkIn(),
                domain.checkOut(),
                domain.guestCount(),
                domain.totalAmount().amount(),
                domain.status().name(),
                domain.reservationId(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public ReservationSession toDomain(ReservationSessionJpaEntity entity) {
        return ReservationSession.reconstitute(
                ReservationSessionId.of(entity.getId()),
                entity.getIdempotencyKey(),
                PropertyId.of(entity.getPropertyId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                RatePlanId.of(entity.getRatePlanId()),
                entity.getCheckIn(),
                entity.getCheckOut(),
                entity.getGuestCount(),
                Money.of(entity.getTotalAmount()),
                ReservationSessionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getReservationId()
        );
    }
}

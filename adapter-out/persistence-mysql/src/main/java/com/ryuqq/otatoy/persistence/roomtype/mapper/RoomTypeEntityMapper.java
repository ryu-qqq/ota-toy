package com.ryuqq.otatoy.persistence.roomtype.mapper;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeStatus;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * RoomType Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RoomTypeJpaEntity toEntity(RoomType domain) {
        return RoomTypeJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.propertyId().value(),
                domain.name().value(),
                domain.description() != null ? domain.description().value() : null,
                domain.areaSqm(),
                domain.areaPyeong(),
                domain.baseOccupancy(),
                domain.maxOccupancy(),
                domain.baseInventory(),
                domain.checkInTime() != null ? domain.checkInTime().toString() : null,
                domain.checkOutTime() != null ? domain.checkOutTime().toString() : null,
                domain.status().name(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RoomType toDomain(RoomTypeJpaEntity entity) {
        return RoomType.reconstitute(
                RoomTypeId.of(entity.getId()),
                PropertyId.of(entity.getPropertyId()),
                RoomTypeName.of(entity.getName()),
                entity.getDescription() != null ? RoomTypeDescription.of(entity.getDescription()) : null,
                entity.getAreaSqm(),
                entity.getAreaPyeong(),
                entity.getBaseOccupancy(),
                entity.getMaxOccupancy(),
                entity.getBaseInventory(),
                entity.getCheckInTime() != null ? LocalTime.parse(entity.getCheckInTime()) : null,
                entity.getCheckOutTime() != null ? LocalTime.parse(entity.getCheckOutTime()) : null,
                RoomTypeStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

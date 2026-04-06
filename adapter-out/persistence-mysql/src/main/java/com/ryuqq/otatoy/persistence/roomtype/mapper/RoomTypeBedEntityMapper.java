package com.ryuqq.otatoy.persistence.roomtype.mapper;

import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBedId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeBedJpaEntity;
import org.springframework.stereotype.Component;

/**
 * RoomTypeBed Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeBedEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RoomTypeBedJpaEntity toEntity(RoomTypeBed domain) {
        return RoomTypeBedJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.roomTypeId().value(),
                domain.bedTypeId().value(),
                domain.quantity(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RoomTypeBed toDomain(RoomTypeBedJpaEntity entity) {
        return RoomTypeBed.reconstitute(
                RoomTypeBedId.of(entity.getId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                BedTypeId.of(entity.getBedTypeId()),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

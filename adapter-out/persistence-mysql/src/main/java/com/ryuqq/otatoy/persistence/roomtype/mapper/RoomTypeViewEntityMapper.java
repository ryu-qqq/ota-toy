package com.ryuqq.otatoy.persistence.roomtype.mapper;

import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeViewId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeViewJpaEntity;
import org.springframework.stereotype.Component;

/**
 * RoomTypeView Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeViewEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RoomTypeViewJpaEntity toEntity(RoomTypeView domain) {
        return RoomTypeViewJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.roomTypeId().value(),
                domain.viewTypeId().value(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RoomTypeView toDomain(RoomTypeViewJpaEntity entity) {
        return RoomTypeView.reconstitute(
                RoomTypeViewId.of(entity.getId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                ViewTypeId.of(entity.getViewTypeId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

package com.ryuqq.otatoy.persistence.inventory.mapper;

import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Inventory Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class InventoryEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public InventoryJpaEntity toEntity(Inventory domain) {
        return InventoryJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.roomTypeId().value(),
                domain.inventoryDate(),
                domain.totalInventory(),
                domain.availableCount(),
                domain.isStopSell(),
                domain.version(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public Inventory toDomain(InventoryJpaEntity entity) {
        return Inventory.reconstitute(
                InventoryId.of(entity.getId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                entity.getInventoryDate(),
                entity.getTotalInventory(),
                entity.getAvailableCount(),
                entity.isStopSell(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

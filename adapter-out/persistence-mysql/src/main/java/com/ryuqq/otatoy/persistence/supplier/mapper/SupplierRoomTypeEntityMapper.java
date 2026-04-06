package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.supplier.SupplierMappingStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierPropertyId;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierRoomTypeJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SupplierRoomTypeEntityMapper {

    public SupplierRoomType toDomain(SupplierRoomTypeJpaEntity entity) {
        return SupplierRoomType.reconstitute(
                SupplierRoomTypeId.of(entity.getId()),
                SupplierPropertyId.of(entity.getSupplierPropertyId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                entity.getSupplierRoomCode(),
                entity.getLastSyncedAt(),
                SupplierMappingStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

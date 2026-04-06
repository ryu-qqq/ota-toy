package com.ryuqq.otatoy.persistence.inventory.adapter;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryQueryPort;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.inventory.mapper.InventoryEntityMapper;
import com.ryuqq.otatoy.persistence.inventory.repository.InventoryQueryDslRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Inventory Query Adapter.
 * InventoryQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryQueryAdapter implements InventoryQueryPort {

    private final InventoryQueryDslRepository queryDslRepository;
    private final InventoryEntityMapper mapper;

    public InventoryQueryAdapter(InventoryQueryDslRepository queryDslRepository, InventoryEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Inventory> findByRoomTypeIdsAndDateRange(List<RoomTypeId> roomTypeIds,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
        List<Long> ids = roomTypeIds.stream()
                .map(RoomTypeId::value)
                .toList();
        return queryDslRepository.findByRoomTypeIdsAndDateRange(ids, startDate, endDate).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

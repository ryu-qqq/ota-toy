package com.ryuqq.otatoy.persistence.roomtype.adapter;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * RoomType Query Adapter.
 * RoomTypeQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeQueryAdapter implements RoomTypeQueryPort {

    private final RoomTypeQueryDslRepository queryDslRepository;
    private final RoomTypeEntityMapper mapper;

    public RoomTypeQueryAdapter(RoomTypeQueryDslRepository queryDslRepository, RoomTypeEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RoomType> findById(RoomTypeId id) {
        return queryDslRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(RoomTypeId id) {
        return queryDslRepository.existsById(id.value());
    }

    @Override
    public List<RoomType> findByPropertyId(PropertyId propertyId) {
        return queryDslRepository.findByPropertyId(propertyId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RoomType> findActiveByPropertyIdAndMinOccupancy(PropertyId propertyId, int minOccupancy) {
        return queryDslRepository.findActiveByPropertyIdAndMinOccupancy(propertyId.value(), minOccupancy).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

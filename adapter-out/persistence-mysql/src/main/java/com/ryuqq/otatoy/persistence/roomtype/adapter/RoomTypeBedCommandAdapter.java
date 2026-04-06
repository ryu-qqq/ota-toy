package com.ryuqq.otatoy.persistence.roomtype.adapter;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeBedCommandPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeBedJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeBedEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeBedJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RoomTypeBed Command Adapter.
 * RoomTypeBedCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeBedCommandAdapter implements RoomTypeBedCommandPort {

    private final RoomTypeBedJpaRepository jpaRepository;
    private final RoomTypeBedEntityMapper mapper;

    public RoomTypeBedCommandAdapter(RoomTypeBedJpaRepository jpaRepository,
                                      RoomTypeBedEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persistAll(List<RoomTypeBed> beds) {
        List<RoomTypeBedJpaEntity> entities = beds.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

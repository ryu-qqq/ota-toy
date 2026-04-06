package com.ryuqq.otatoy.persistence.roomtype.adapter;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeCommandPort;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeJpaRepository;
import org.springframework.stereotype.Component;

/**
 * RoomType Command Adapter.
 * RoomTypeCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeCommandAdapter implements RoomTypeCommandPort {

    private final RoomTypeJpaRepository jpaRepository;
    private final RoomTypeEntityMapper mapper;

    public RoomTypeCommandAdapter(RoomTypeJpaRepository jpaRepository, RoomTypeEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(RoomType roomType) {
        RoomTypeJpaEntity entity = mapper.toEntity(roomType);
        RoomTypeJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }
}

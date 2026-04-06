package com.ryuqq.otatoy.persistence.roomtype.adapter;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeViewCommandPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeViewJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeViewEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeViewJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RoomTypeView Command Adapter.
 * RoomTypeViewCommandPort를 구현하며, JpaRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RoomTypeViewCommandAdapter implements RoomTypeViewCommandPort {

    private final RoomTypeViewJpaRepository jpaRepository;
    private final RoomTypeViewEntityMapper mapper;

    public RoomTypeViewCommandAdapter(RoomTypeViewJpaRepository jpaRepository,
                                       RoomTypeViewEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persistAll(List<RoomTypeView> views) {
        List<RoomTypeViewJpaEntity> entities = views.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}

package com.ryuqq.otatoy.application.roomtype.facade;

import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeBedCommandPort;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeCommandPort;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeViewCommandPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RoomType + RoomTypeBed + RoomTypeView 영속화 묶음 (APP-FCD-001).
 * 여러 CommandPort를 하나의 트랜잭션에서 원자적으로 저장한다.
 * 저장만 담당하며, 객체 생성은 Factory의 책임이다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RoomTypePersistenceFacade {

    private final RoomTypeCommandPort roomTypeCommandPort;
    private final RoomTypeBedCommandPort roomTypeBedCommandPort;
    private final RoomTypeViewCommandPort roomTypeViewCommandPort;

    public RoomTypePersistenceFacade(RoomTypeCommandPort roomTypeCommandPort,
                                     RoomTypeBedCommandPort roomTypeBedCommandPort,
                                     RoomTypeViewCommandPort roomTypeViewCommandPort) {
        this.roomTypeCommandPort = roomTypeCommandPort;
        this.roomTypeBedCommandPort = roomTypeBedCommandPort;
        this.roomTypeViewCommandPort = roomTypeViewCommandPort;
    }

    /**
     * RoomType을 저장한 뒤, 할당된 ID로 Bed/View에 roomTypeId를 부여하여 함께 저장한다.
     * 세 저장이 하나의 트랜잭션에서 원자적으로 실행된다.
     */
    @Transactional
    public Long persist(RoomTypeBundle bundle) {
        Long roomTypeId = roomTypeCommandPort.persist(bundle.roomType());
        RoomTypeId assignedId = RoomTypeId.of(roomTypeId);

        if (!bundle.beds().isEmpty()) {
            List<RoomTypeBed> beds = bundle.beds().stream()
                .map(bed -> bed.withRoomTypeId(assignedId))
                .toList();
            roomTypeBedCommandPort.persistAll(beds);
        }

        if (!bundle.views().isEmpty()) {
            List<RoomTypeView> views = bundle.views().stream()
                .map(view -> view.withRoomTypeId(assignedId))
                .toList();
            roomTypeViewCommandPort.persistAll(views);
        }

        return roomTypeId;
    }
}

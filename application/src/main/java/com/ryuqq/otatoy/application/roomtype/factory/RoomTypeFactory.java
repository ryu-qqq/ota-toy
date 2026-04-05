package com.ryuqq.otatoy.application.roomtype.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * RoomType 관련 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RoomTypeFactory {

    private final TimeProvider timeProvider;

    public RoomTypeFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * RoomType + Bed + View 번들을 생성한다.
     * Bed/View는 roomTypeId = null로 생성된다.
     * PersistenceFacade에서 RoomType 저장 후 withRoomTypeId()로 ID를 할당한다.
     */
    public RoomTypeBundle createBundle(RegisterRoomTypeCommand command) {
        Instant now = timeProvider.now();

        RoomType roomType = RoomType.forNew(
            command.propertyId(),
            command.name(),
            command.description(),
            command.areaSqm(),
            command.areaPyeong(),
            command.baseOccupancy(),
            command.maxOccupancy(),
            command.baseInventory(),
            command.checkInTime(),
            command.checkOutTime(),
            now
        );

        List<RoomTypeBed> beds = command.beds() != null
            ? command.beds().stream()
                .map(item -> RoomTypeBed.forPending(item.bedTypeId(), item.quantity(), now))
                .toList()
            : List.of();

        List<RoomTypeView> views = command.views() != null
            ? command.views().stream()
                .map(item -> RoomTypeView.forPending(item.viewTypeId(), now))
                .toList()
            : List.of();

        return new RoomTypeBundle(roomType, beds, views);
    }
}

package com.ryuqq.otatoy.application.roomtype.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * RegisterRoomTypeCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RegisterRoomTypeCommandFixture {

    private RegisterRoomTypeCommandFixture() {}

    /**
     * 기본 객실 유형 등록 커맨드 (침대 2종 + 전망 1종)
     */
    public static RegisterRoomTypeCommand aRegisterRoomTypeCommand() {
        return new RegisterRoomTypeCommand(
            PropertyId.of(1L),
            RoomTypeName.of("디럭스 더블"),
            RoomTypeDescription.of("넓은 객실"),
            BigDecimal.valueOf(33.0),
            "10평",
            2, 4, 5,
            LocalTime.of(15, 0),
            LocalTime.of(11, 0),
            List.of(
                new RegisterRoomTypeCommand.BedItem(BedTypeId.of(1L), 1),
                new RegisterRoomTypeCommand.BedItem(BedTypeId.of(2L), 2)
            ),
            List.of(
                new RegisterRoomTypeCommand.ViewItem(ViewTypeId.of(1L))
            )
        );
    }
}

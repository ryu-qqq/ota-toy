package com.ryuqq.otatoy.application.roomtype.port.in;

import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;

/**
 * 객실 유형 등록 UseCase (Inbound Port).
 * Controller가 호출하는 진입점이다 (APP-UC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RegisterRoomTypeUseCase {

    Long execute(RegisterRoomTypeCommand command);
}

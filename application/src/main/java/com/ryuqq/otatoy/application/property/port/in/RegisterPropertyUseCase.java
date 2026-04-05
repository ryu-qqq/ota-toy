package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;

/**
 * 숙소 기본정보 등록 UseCase (Inbound Port).
 * Controller가 호출하는 진입점이다 (APP-UC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RegisterPropertyUseCase {

    Long execute(RegisterPropertyCommand command);
}

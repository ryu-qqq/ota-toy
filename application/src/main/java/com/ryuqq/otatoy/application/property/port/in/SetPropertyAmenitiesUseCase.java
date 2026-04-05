package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;

/**
 * 숙소 편의시설 설정 UseCase (Inbound Port).
 * 편의시설은 전체 교체(replace) 방식으로 설정된다 (APP-UC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface SetPropertyAmenitiesUseCase {

    void execute(SetPropertyAmenitiesCommand command);
}

package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;

/**
 * 숙소 속성값 설정 UseCase (Inbound Port).
 * 전체 교체(replace) 방식으로 속성값을 설정한다.
 * Controller가 호출하는 진입점이다 (APP-UC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface SetPropertyAttributesUseCase {

    void execute(SetPropertyAttributesCommand command);
}

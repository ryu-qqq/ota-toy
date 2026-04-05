package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;

/**
 * 숙소 사진 설정 UseCase (Inbound Port).
 * diff 패턴으로 기존/신규를 비교하여 추가/삭제/유지를 처리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SetPropertyPhotosUseCase {

    void execute(SetPropertyPhotosCommand command);
}

package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * SetPropertyPhotosCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class SetPropertyPhotosCommandFixture {

    private SetPropertyPhotosCommandFixture() {}

    /**
     * 기본 사진 설정 커맨드 (EXTERIOR + LOBBY 2장)
     */
    public static SetPropertyPhotosCommand aSetPropertyPhotosCommand() {
        return SetPropertyPhotosCommand.of(
            PropertyId.of(1L),
            List.of(
                SetPropertyPhotosCommand.PhotoItem.of(PhotoType.EXTERIOR,
                    OriginUrl.of("https://origin.com/1.jpg"), CdnUrl.of("https://cdn.com/1.jpg"), 1),
                SetPropertyPhotosCommand.PhotoItem.of(PhotoType.LOBBY,
                    OriginUrl.of("https://origin.com/2.jpg"), CdnUrl.of("https://cdn.com/2.jpg"), 2)
            )
        );
    }
}

package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * 숙소 사진 설정 요청 Command.
 * diff 패턴으로 기존/신규를 비교하여 추가/삭제/유지를 처리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SetPropertyPhotosCommand(
    PropertyId propertyId,
    List<PhotoItem> photos
) {

    public record PhotoItem(
        PhotoType photoType,
        OriginUrl originUrl,
        CdnUrl cdnUrl,
        int sortOrder
    ) {
        public static PhotoItem of(PhotoType photoType, OriginUrl originUrl, CdnUrl cdnUrl, int sortOrder) {
            return new PhotoItem(photoType, originUrl, cdnUrl, sortOrder);
        }
    }

    public static SetPropertyPhotosCommand of(PropertyId propertyId, List<PhotoItem> photos) {
        return new SetPropertyPhotosCommand(propertyId, photos);
    }
}

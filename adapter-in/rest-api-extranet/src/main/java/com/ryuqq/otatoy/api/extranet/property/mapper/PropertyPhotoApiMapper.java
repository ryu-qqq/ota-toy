package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyPhotosApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * 사진 API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(String, int)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class PropertyPhotoApiMapper {

    private PropertyPhotoApiMapper() {}

    /**
     * 사진 설정 API 요청을 Application Command로 변환한다.
     *
     * @param propertyId 숙소 ID (PathVariable)
     * @param request    사진 설정 요청 DTO
     * @return 사진 설정 Command
     */
    public static SetPropertyPhotosCommand toCommand(Long propertyId, SetPropertyPhotosApiRequest request) {
        return SetPropertyPhotosCommand.of(
            PropertyId.of(propertyId),
            request.photos().stream()
                .map(photo -> SetPropertyPhotosCommand.PhotoItem.of(
                    PhotoType.valueOf(photo.photoType()),
                    OriginUrl.of(photo.originUrl()),
                    photo.cdnUrl() != null ? CdnUrl.of(photo.cdnUrl()) : null,
                    photo.sortOrder()
                ))
                .toList()
        );
    }
}

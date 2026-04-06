package com.ryuqq.otatoy.api.extranet.property.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * 숙소 사진 설정 요청 DTO.
 * 전체 교체(diff) 방식으로 동작하며, 원시 타입으로 수신한다.
 * {@link com.ryuqq.otatoy.api.extranet.property.mapper.PropertyPhotoApiMapper}에서
 * Domain VO로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SetPropertyPhotosApiRequest(

    @NotEmpty(message = "사진 목록은 필수입니다")
    List<@Valid PhotoItem> photos
) {

    /**
     * 개별 사진 항목.
     *
     * @param photoType 사진 유형 코드 (예: EXTERIOR, LOBBY)
     * @param originUrl 원본 이미지 URL
     * @param cdnUrl    CDN 이미지 URL (선택)
     * @param sortOrder 정렬 순서 (0 이상)
     */
    public record PhotoItem(

        @NotBlank(message = "사진 유형은 필수입니다")
        String photoType,

        @NotBlank(message = "원본 이미지 URL은 필수입니다")
        String originUrl,

        String cdnUrl,

        @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다")
        int sortOrder
    ) {}
}

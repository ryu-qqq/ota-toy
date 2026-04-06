package com.ryuqq.otatoy.api.extranet.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 숙소 기본정보 등록 요청 DTO.
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.property.mapper.PropertyApiMapper}에서
 * Domain VO로 변환한다.
 * <p>
 * 편의시설/사진/속성값은 별도 API로 분리하므로 이 요청에 포함하지 않는다 (AC-4).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterPropertyApiRequest(

    @NotNull(message = "파트너 ID는 필수입니다")
    Long partnerId,

    Long brandId,

    @NotNull(message = "숙소 유형 ID는 필수입니다")
    Long propertyTypeId,

    @NotBlank(message = "숙소 이름은 필수입니다")
    @Size(max = 100, message = "숙소 이름은 100자 이하입니다")
    String name,

    @Size(max = 2000, message = "숙소 설명은 2000자 이하입니다")
    String description,

    @NotBlank(message = "주소는 필수입니다")
    String address,

    double latitude,

    double longitude,

    String neighborhood,

    String region,

    @Size(max = 500, message = "홍보 문구는 500자 이하입니다")
    String promotionText
) {}

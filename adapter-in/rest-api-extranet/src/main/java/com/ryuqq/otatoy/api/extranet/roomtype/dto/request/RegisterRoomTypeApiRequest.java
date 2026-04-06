package com.ryuqq.otatoy.api.extranet.roomtype.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 객실 유형 등록 요청 DTO.
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.roomtype.mapper.RoomTypeApiMapper}에서
 * Domain VO로 변환한다.
 * <p>
 * baseOccupancy > maxOccupancy 검증은 Domain에서 수행한다 (AC-6).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterRoomTypeApiRequest(

    @NotBlank(message = "객실 이름은 필수입니다")
    @Size(max = 100, message = "객실 이름은 100자 이하입니다")
    String name,

    @Size(max = 2000, message = "객실 설명은 2000자 이하입니다")
    String description,

    @NotNull(message = "면적(평방미터)은 필수입니다")
    @PositiveOrZero(message = "면적은 0 이상이어야 합니다")
    BigDecimal areaSqm,

    String areaPyeong,

    @Positive(message = "기본 투숙 인원은 1 이상이어야 합니다")
    int baseOccupancy,

    @Positive(message = "최대 투숙 인원은 1 이상이어야 합니다")
    int maxOccupancy,

    @PositiveOrZero(message = "기본 재고는 0 이상이어야 합니다")
    int baseInventory,

    @NotBlank(message = "체크인 시간은 필수입니다")
    String checkInTime,

    @NotBlank(message = "체크아웃 시간은 필수입니다")
    String checkOutTime,

    List<@Valid BedItem> beds,

    List<@Valid ViewItem> views
) {

    /**
     * 침대 구성 항목.
     *
     * @param bedTypeId 침대 유형 ID
     * @param quantity  수량
     */
    public record BedItem(

        @NotNull(message = "침대 유형 ID는 필수입니다")
        Long bedTypeId,

        @Positive(message = "침대 수량은 1 이상이어야 합니다")
        int quantity
    ) {}

    /**
     * 전망 항목.
     *
     * @param viewTypeId 전망 유형 ID
     */
    public record ViewItem(

        @NotNull(message = "전망 유형 ID는 필수입니다")
        Long viewTypeId
    ) {}
}

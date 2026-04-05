package com.ryuqq.otatoy.application.roomtype.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * 객실 유형 등록 요청 Command.
 * 필드 타입은 Domain VO를 사용한다 (APP-DTO-001).
 * BedItem, ViewItem은 내부 record로 선언한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterRoomTypeCommand(
    PropertyId propertyId,
    RoomTypeName name,
    RoomTypeDescription description,
    BigDecimal areaSqm,
    String areaPyeong,
    int baseOccupancy,
    int maxOccupancy,
    int baseInventory,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    List<BedItem> beds,
    List<ViewItem> views
) {

    /**
     * 침대 구성 항목.
     */
    public record BedItem(
        BedTypeId bedTypeId,
        int quantity
    ) {}

    /**
     * 전망 항목.
     */
    public record ViewItem(
        ViewTypeId viewTypeId
    ) {}
}

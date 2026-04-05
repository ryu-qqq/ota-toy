package com.ryuqq.otatoy.api.extranet.roomtype.mapper;

import com.ryuqq.otatoy.api.extranet.roomtype.dto.RegisterRoomTypeApiRequest;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import java.time.LocalTime;
import java.util.List;

/**
 * 객실 유형 API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(Long, String 등)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RoomTypeApiMapper {

    private RoomTypeApiMapper() {}

    /**
     * 객실 유형 등록 API 요청을 Application Command로 변환한다.
     *
     * @param propertyId 숙소 ID (PathVariable)
     * @param request    객실 유형 등록 요청 DTO
     * @return 객실 유형 등록 Command
     */
    public static RegisterRoomTypeCommand toCommand(Long propertyId, RegisterRoomTypeApiRequest request) {
        List<RegisterRoomTypeCommand.BedItem> beds = request.beds() != null
            ? request.beds().stream()
                .map(bed -> new RegisterRoomTypeCommand.BedItem(
                    BedTypeId.of(bed.bedTypeId()),
                    bed.quantity()
                ))
                .toList()
            : List.of();

        List<RegisterRoomTypeCommand.ViewItem> views = request.views() != null
            ? request.views().stream()
                .map(view -> new RegisterRoomTypeCommand.ViewItem(
                    ViewTypeId.of(view.viewTypeId())
                ))
                .toList()
            : List.of();

        return new RegisterRoomTypeCommand(
            PropertyId.of(propertyId),
            RoomTypeName.of(request.name()),
            request.description() != null ? RoomTypeDescription.of(request.description()) : null,
            request.areaSqm(),
            request.areaPyeong(),
            request.baseOccupancy(),
            request.maxOccupancy(),
            request.baseInventory(),
            LocalTime.parse(request.checkInTime()),
            LocalTime.parse(request.checkOutTime()),
            beds,
            views
        );
    }
}

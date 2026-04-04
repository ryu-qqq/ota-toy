package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeView(
        Long id,
        RoomTypeId roomTypeId,
        Long viewTypeId
) {

    public static RoomTypeView of(RoomTypeId roomTypeId, Long viewTypeId) {
        return new RoomTypeView(null, roomTypeId, viewTypeId);
    }

    public static RoomTypeView reconstitute(Long id, RoomTypeId roomTypeId, Long viewTypeId) {
        return new RoomTypeView(id, roomTypeId, viewTypeId);
    }
}

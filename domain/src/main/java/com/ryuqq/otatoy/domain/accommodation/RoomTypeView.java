package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeView(
        Long id,
        RoomTypeId roomTypeId,
        Long viewTypeId
) {

    public RoomTypeView {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (viewTypeId == null) {
            throw new IllegalArgumentException("전망 유형 ID는 필수입니다");
        }
    }

    public static RoomTypeView of(RoomTypeId roomTypeId, Long viewTypeId) {
        return new RoomTypeView(null, roomTypeId, viewTypeId);
    }

    public static RoomTypeView reconstitute(Long id, RoomTypeId roomTypeId, Long viewTypeId) {
        return new RoomTypeView(id, roomTypeId, viewTypeId);
    }
}

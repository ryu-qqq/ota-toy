package com.ryuqq.otatoy.domain.accommodation;

public record RoomTypeBed(
        Long id,
        RoomTypeId roomTypeId,
        Long bedTypeId,
        int quantity
) {

    public RoomTypeBed {
        if (quantity <= 0) {
            throw new IllegalArgumentException("침대 수량은 1개 이상이어야 합니다");
        }
    }

    public static RoomTypeBed of(RoomTypeId roomTypeId, Long bedTypeId, int quantity) {
        return new RoomTypeBed(null, roomTypeId, bedTypeId, quantity);
    }

    public static RoomTypeBed reconstitute(Long id, RoomTypeId roomTypeId, Long bedTypeId, int quantity) {
        return new RoomTypeBed(id, roomTypeId, bedTypeId, quantity);
    }
}

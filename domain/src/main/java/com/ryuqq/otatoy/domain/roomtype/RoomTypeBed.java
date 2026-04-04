package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;

import java.util.Objects;

/**
 * 객실의 침대 구성을 나타내는 엔티티.
 * 침대 유형과 수량을 관리한다.
 */
public class RoomTypeBed {

    private final RoomTypeBedId id;
    private final RoomTypeId roomTypeId;
    private final BedTypeId bedTypeId;
    private final int quantity;

    private RoomTypeBed(RoomTypeBedId id, RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.bedTypeId = bedTypeId;
        this.quantity = quantity;
    }

    public static RoomTypeBed forNew(RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity) {
        validate(bedTypeId, quantity);
        return new RoomTypeBed(RoomTypeBedId.of(null), roomTypeId, bedTypeId, quantity);
    }

    private static void validate(BedTypeId bedTypeId, int quantity) {
        if (bedTypeId == null || bedTypeId.value() == null) {
            throw new IllegalArgumentException("침대 유형 ID는 필수입니다");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("침대 수량은 1개 이상이어야 합니다");
        }
    }

    public static RoomTypeBed reconstitute(RoomTypeBedId id, RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity) {
        return new RoomTypeBed(id, roomTypeId, bedTypeId, quantity);
    }

    public RoomTypeBedId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public BedTypeId bedTypeId() { return bedTypeId; }
    public int quantity() { return quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomTypeBed r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

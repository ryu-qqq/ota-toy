package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class RoomTypeView {

    private final RoomTypeViewId id;
    private final RoomTypeId roomTypeId;
    private final ViewTypeId viewTypeId;

    private RoomTypeView(RoomTypeViewId id, RoomTypeId roomTypeId, ViewTypeId viewTypeId) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.viewTypeId = viewTypeId;
    }

    public static RoomTypeView forNew(RoomTypeId roomTypeId, ViewTypeId viewTypeId) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (viewTypeId == null || viewTypeId.value() == null) {
            throw new IllegalArgumentException("전망 유형 ID는 필수입니다");
        }
        return new RoomTypeView(RoomTypeViewId.of(null), roomTypeId, viewTypeId);
    }

    public static RoomTypeView reconstitute(RoomTypeViewId id, RoomTypeId roomTypeId, ViewTypeId viewTypeId) {
        return new RoomTypeView(id, roomTypeId, viewTypeId);
    }

    public RoomTypeViewId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public ViewTypeId viewTypeId() { return viewTypeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomTypeView r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;

import java.time.Instant;
import java.util.Objects;

/**
 * 객실과 전망 유형의 매핑을 나타내는 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class RoomTypeView {

    private final RoomTypeViewId id;
    private final RoomTypeId roomTypeId;
    private final ViewTypeId viewTypeId;
    private final Instant createdAt;
    private Instant updatedAt;

    private RoomTypeView(RoomTypeViewId id, RoomTypeId roomTypeId, ViewTypeId viewTypeId,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.viewTypeId = viewTypeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RoomTypeView forNew(RoomTypeId roomTypeId, ViewTypeId viewTypeId, Instant now) {
        validate(roomTypeId, viewTypeId);
        return new RoomTypeView(RoomTypeViewId.of(null), roomTypeId, viewTypeId, now, now);
    }

    private static void validate(RoomTypeId roomTypeId, ViewTypeId viewTypeId) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (viewTypeId == null || viewTypeId.value() == null) {
            throw new IllegalArgumentException("전망 유형 ID는 필수입니다");
        }
    }

    public static RoomTypeView reconstitute(RoomTypeViewId id, RoomTypeId roomTypeId, ViewTypeId viewTypeId,
                                               Instant createdAt, Instant updatedAt) {
        return new RoomTypeView(id, roomTypeId, viewTypeId, createdAt, updatedAt);
    }

    public RoomTypeViewId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public ViewTypeId viewTypeId() { return viewTypeId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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

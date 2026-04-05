package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;

import java.time.Instant;
import java.util.Objects;

/**
 * 객실의 침대 구성을 나타내는 엔티티.
 * 침대 유형과 수량을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class RoomTypeBed {

    private final RoomTypeBedId id;
    private final RoomTypeId roomTypeId;
    private final BedTypeId bedTypeId;
    private final int quantity;
    private final Instant createdAt;
    private Instant updatedAt;

    private RoomTypeBed(RoomTypeBedId id, RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.bedTypeId = bedTypeId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RoomTypeBed forNew(RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity, Instant now) {
        validate(bedTypeId, quantity);
        return new RoomTypeBed(RoomTypeBedId.of(null), roomTypeId, bedTypeId, quantity, now, now);
    }

    /**
     * 부모(RoomType) ID가 아직 할당되지 않은 상태로 생성한다.
     * PersistenceFacade에서 withRoomTypeId()로 ID를 할당한다.
     */
    public static RoomTypeBed forPending(BedTypeId bedTypeId, int quantity, Instant now) {
        validate(bedTypeId, quantity);
        return new RoomTypeBed(RoomTypeBedId.of(null), null, bedTypeId, quantity, now, now);
    }

    private static void validate(BedTypeId bedTypeId, int quantity) {
        if (bedTypeId == null || bedTypeId.value() == null) {
            throw new IllegalArgumentException("침대 유형 ID는 필수입니다");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("침대 수량은 1개 이상이어야 합니다");
        }
    }

    public static RoomTypeBed reconstitute(RoomTypeBedId id, RoomTypeId roomTypeId, BedTypeId bedTypeId, int quantity,
                                              Instant createdAt, Instant updatedAt) {
        return new RoomTypeBed(id, roomTypeId, bedTypeId, quantity, createdAt, updatedAt);
    }

    /**
     * roomTypeId를 할당한 새 객체를 반환한다.
     * 원본은 변경되지 않는다 (불변 복사).
     */
    public RoomTypeBed withRoomTypeId(RoomTypeId roomTypeId) {
        return new RoomTypeBed(this.id, roomTypeId, this.bedTypeId, this.quantity, this.createdAt, this.updatedAt);
    }

    public RoomTypeBedId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public BedTypeId bedTypeId() { return bedTypeId; }
    public int quantity() { return quantity; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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

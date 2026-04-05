package com.ryuqq.otatoy.application.roomtype.dto;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;

import java.util.List;

/**
 * RoomTypeBundle 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RoomTypeBundleFixture {

    private RoomTypeBundleFixture() {}

    /**
     * 기본 번들 (RoomType + 침대 2종 + 전망 1종)
     */
    public static RoomTypeBundle aRoomTypeBundle() {
        return new RoomTypeBundle(
            RoomTypeFixture.aRoomType(),
            RoomTypeFixture.defaultPendingBeds(),
            RoomTypeFixture.defaultPendingViews()
        );
    }

    /**
     * 침대/전망 없는 빈 번들
     */
    public static RoomTypeBundle anEmptyRoomTypeBundle() {
        return new RoomTypeBundle(
            RoomTypeFixture.aRoomTypeWithName("스탠다드"),
            List.of(),
            List.of()
        );
    }
}

package com.ryuqq.otatoy.application.roomtype.port.out;

import com.ryuqq.otatoy.domain.roomtype.RoomType;

/**
 * RoomType 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RoomTypeCommandPort {

    Long persist(RoomType roomType);
}

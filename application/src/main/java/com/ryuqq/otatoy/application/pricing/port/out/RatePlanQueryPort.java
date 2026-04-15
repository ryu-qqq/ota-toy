package com.ryuqq.otatoy.application.pricing.port.out;

import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.util.List;
import java.util.Optional;

/**
 * RatePlan 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RatePlanQueryPort {

    Optional<RatePlan> findById(RatePlanId id);

    boolean existsById(RatePlanId id);

    /**
     * 특정 객실 유형들에 연결된 요금 정책 목록을 조회한다.
     * 요금 조회 시 해당 숙소의 모든 객실 유형별 RatePlan을 가져오는 데 사용.
     */
    List<RatePlan> findByRoomTypeIds(List<RoomTypeId> roomTypeIds);
}

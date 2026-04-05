package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanQueryPort;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RatePlan 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RatePlanReadManager {

    private final RatePlanQueryPort ratePlanQueryPort;

    public RatePlanReadManager(RatePlanQueryPort ratePlanQueryPort) {
        this.ratePlanQueryPort = ratePlanQueryPort;
    }

    /**
     * ID로 요금 정책을 조회한다. 존재하지 않으면 RatePlanNotFoundException 발생.
     */
    @Transactional(readOnly = true)
    public RatePlan getById(RatePlanId id) {
        return ratePlanQueryPort.findById(id)
            .orElseThrow(RatePlanNotFoundException::new);
    }

    /**
     * 요금 정책 존재 여부를 확인한다. 존재하지 않으면 RatePlanNotFoundException 발생.
     */
    @Transactional(readOnly = true)
    public void verifyExists(RatePlanId id) {
        if (!ratePlanQueryPort.existsById(id)) {
            throw new RatePlanNotFoundException();
        }
    }

    /**
     * 특정 객실 유형들에 연결된 요금 정책 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public RatePlans findByRoomTypeIds(List<RoomTypeId> roomTypeIds) {
        return RatePlans.of(ratePlanQueryPort.findByRoomTypeIds(roomTypeIds));
    }
}

package com.ryuqq.otatoy.application.roomtype.manager;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RoomType 조회 트랜잭션 경계 관리자.
 * 다른 BC에서도 호출 가능한 ReadManager (APP-BC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RoomTypeReadManager {

    private final RoomTypeQueryPort roomTypeQueryPort;

    public RoomTypeReadManager(RoomTypeQueryPort roomTypeQueryPort) {
        this.roomTypeQueryPort = roomTypeQueryPort;
    }

    @Transactional(readOnly = true)
    public RoomType getById(RoomTypeId id) {
        return roomTypeQueryPort.findById(id)
            .orElseThrow(RoomTypeNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(RoomTypeId id) {
        if (!roomTypeQueryPort.existsById(id)) {
            throw new RoomTypeNotFoundException();
        }
    }

    /**
     * 특정 숙소에 속한 객실 유형 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<RoomType> findByPropertyId(PropertyId propertyId) {
        return roomTypeQueryPort.findByPropertyId(propertyId);
    }
}

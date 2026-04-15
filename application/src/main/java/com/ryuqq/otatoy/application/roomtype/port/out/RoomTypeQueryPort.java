package com.ryuqq.otatoy.application.roomtype.port.out;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.util.List;
import java.util.Optional;

/**
 * RoomType 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RoomTypeQueryPort {

    Optional<RoomType> findById(RoomTypeId id);

    boolean existsById(RoomTypeId id);

    /**
     * 특정 숙소에 속한 객실 유형 목록을 조회한다.
     * 요금 조회 시 해당 숙소의 모든 객실 유형을 가져오는 데 사용.
     */
    List<RoomType> findByPropertyId(PropertyId propertyId);

    /**
     * 특정 숙소의 활성 객실 유형 중 최소 인원 이상 수용 가능한 목록을 조회한다.
     * 고객 요금 조회 시 인원 필터링에 사용.
     */
    List<RoomType> findActiveByPropertyIdAndMinOccupancy(PropertyId propertyId, int minOccupancy);
}

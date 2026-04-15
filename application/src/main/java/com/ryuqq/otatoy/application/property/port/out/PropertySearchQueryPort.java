package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

/**
 * 고객 숙소 검색 전용 Outbound Port.
 * 여러 BC(Property + RoomType + Inventory + Rate)를 조합한 크로스 BC 검색 쿼리를 Adapter에서 구현한다.
 * Port는 도메인 객체만 반환한다 (APP-PRT-002).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface PropertySearchQueryPort {

    /**
     * 검색 조건에 맞는 숙소 목록을 커서 기반으로 조회한다.
     * 최저 가격 계산, 재고 가용성, 인원 필터를 Adapter(쿼리)에서 처리한다.
     *
     * @param criteria 검색 조건 (Domain Criteria)
     * @return 도메인 객체 기반 슬라이스 결과
     */
    SliceResult<Property> searchByCondition(PropertySliceCriteria criteria);
}

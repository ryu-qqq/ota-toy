package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAmenity;

import java.util.List;

/**
 * PropertyAmenity 저장 Outbound Port.
 * 편의시설 persist를 담당한다.
 * id가 null이면 INSERT, 있으면 merge(UPDATE) 처리.
 * 파라미터는 Domain 객체만 사용한다 (APP-PRT-002).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAmenityCommandPort {

    /**
     * 편의시설 목록을 일괄 저장한다.
     * id가 null인 엔티티는 INSERT, id가 있는 엔티티는 merge(UPDATE) 처리.
     */
    void persistAll(List<PropertyAmenity> amenities);
}

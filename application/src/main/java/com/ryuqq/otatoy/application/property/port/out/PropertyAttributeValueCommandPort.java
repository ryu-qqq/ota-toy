package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;

import java.util.List;

/**
 * PropertyAttributeValue 저장 전용 Outbound Port.
 * 속성값 persist를 담당한다.
 * id가 null이면 INSERT, 있으면 merge(UPDATE) 처리.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAttributeValueCommandPort {

    /**
     * 속성값 목록을 일괄 저장한다.
     * id가 null인 엔티티는 INSERT, id가 있는 엔티티는 merge(UPDATE) 처리.
     */
    void persistAll(List<PropertyAttributeValue> attributeValues);
}

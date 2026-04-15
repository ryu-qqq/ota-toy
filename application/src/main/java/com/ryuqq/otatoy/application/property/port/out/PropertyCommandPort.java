package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.Property;

import java.util.List;

/**
 * Property 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyCommandPort {

    Long persist(Property property);

    void persistAll(List<Property> properties);
}

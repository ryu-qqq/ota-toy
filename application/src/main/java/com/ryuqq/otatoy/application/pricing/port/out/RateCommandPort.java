package com.ryuqq.otatoy.application.pricing.port.out;

import com.ryuqq.otatoy.domain.pricing.Rate;

import java.util.List;

/**
 * Rate 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RateCommandPort {

    /**
     * Rate 목록을 일괄 저장한다.
     */
    void persistAll(List<Rate> rates);
}

package com.ryuqq.otatoy.application.pricing.dto;

import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.domain.pricing.RateRule;

import java.util.List;

/**
 * RateRule + RateOverride + Rate + Inventory를 묶는 번들 객체.
 * Factory가 생성하고, PersistenceFacade가 소비한다.
 * RateOverride는 rateRuleId가 null인 pending 상태로 생성된다.
 * PersistenceFacade에서 RateRule 저장 후 withRateRuleId()로 ID를 할당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RateAndInventoryBundle(
    RateRule rateRule,
    List<RateOverride> overrides,
    List<Rate> rates,
    List<Inventory> inventories
) {}

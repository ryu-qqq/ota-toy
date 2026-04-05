package com.ryuqq.otatoy.application.pricing.facade;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.port.out.RateCommandPort;
import com.ryuqq.otatoy.application.pricing.port.out.RateOverrideCommandPort;
import com.ryuqq.otatoy.application.pricing.port.out.RateRuleCommandPort;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.domain.pricing.RateRuleId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RateRule + RateOverride + Rate + Inventory 영속화 묶음 (APP-FCD-001).
 * 여러 CommandPort를 하나의 트랜잭션에서 원자적으로 저장한다 (AC-9).
 * 저장만 담당하며, 객체 생성은 Factory의 책임이다.
 *
 * RateOverride는 pending 상태(rateRuleId=null)로 들어오며,
 * RateRule 저장 후 할당된 ID를 withRateRuleId()로 부여한다 (번들 패턴).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateAndInventoryPersistenceFacade {

    private final RateRuleCommandPort rateRuleCommandPort;
    private final RateOverrideCommandPort rateOverrideCommandPort;
    private final RateCommandPort rateCommandPort;
    private final InventoryCommandPort inventoryCommandPort;

    public RateAndInventoryPersistenceFacade(RateRuleCommandPort rateRuleCommandPort,
                                              RateOverrideCommandPort rateOverrideCommandPort,
                                              RateCommandPort rateCommandPort,
                                              InventoryCommandPort inventoryCommandPort) {
        this.rateRuleCommandPort = rateRuleCommandPort;
        this.rateOverrideCommandPort = rateOverrideCommandPort;
        this.rateCommandPort = rateCommandPort;
        this.inventoryCommandPort = inventoryCommandPort;
    }

    /**
     * RateRule을 저장한 뒤, 할당된 ID로 RateOverride에 rateRuleId를 부여하여 함께 저장한다.
     * Rate, Inventory도 동일 트랜잭션에서 원자적으로 저장된다.
     */
    @Transactional
    public void persist(RateAndInventoryBundle bundle) {
        // 1. RateRule 저장 -> ID 할당
        Long rateRuleId = rateRuleCommandPort.persist(bundle.rateRule());
        RateRuleId assignedRateRuleId = RateRuleId.of(rateRuleId);

        // 2. RateOverride에 rateRuleId 할당 후 저장
        if (!bundle.overrides().isEmpty()) {
            List<RateOverride> overridesWithId = bundle.overrides().stream()
                .map(override -> override.withRateRuleId(assignedRateRuleId))
                .toList();
            rateOverrideCommandPort.persistAll(overridesWithId);
        }

        // 3. Rate 스냅샷 저장
        if (!bundle.rates().isEmpty()) {
            rateCommandPort.persistAll(bundle.rates());
        }

        // 4. Inventory 저장
        if (!bundle.inventories().isEmpty()) {
            inventoryCommandPort.persistAll(bundle.inventories());
        }
    }
}

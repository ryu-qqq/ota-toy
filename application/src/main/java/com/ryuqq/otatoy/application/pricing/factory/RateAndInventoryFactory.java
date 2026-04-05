package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RateRule;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 요금/재고 설정 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 * RateOverride는 forPending()으로 rateRuleId=null 상태로 생성된다.
 * Rate 생성은 도메인(RateRule.generateRates())이 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateAndInventoryFactory {

    private final TimeProvider timeProvider;

    public RateAndInventoryFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * SetRateAndInventoryCommand + RatePlan 정보를 기반으로
     * RateRule + RateOverride + Rate + Inventory 번들을 생성한다.
     *
     * @param command 요금/재고 설정 커맨드
     * @param ratePlan 요금 정책 (roomTypeId 획득용)
     * @return 번들 객체 (PersistenceFacade에서 소비)
     */
    public RateAndInventoryBundle createBundle(SetRateAndInventoryCommand command, RatePlan ratePlan) {
        Instant now = timeProvider.now();
        RoomTypeId roomTypeId = ratePlan.roomTypeId();

        // 1. RateRule 생성
        RateRule rateRule = RateRule.forNew(
            command.ratePlanId(),
            command.startDate(),
            command.endDate(),
            command.basePrice(),
            command.weekdayPrice(),
            command.fridayPrice(),
            command.saturdayPrice(),
            command.sundayPrice(),
            now
        );

        // 2. RateOverride 생성 (pending 상태 -- rateRuleId = null)
        List<RateOverride> overrides = createOverrides(command, rateRule, now);

        // 3. Rate 스냅샷 생성 (도메인이 계산)
        List<Rate> rates = rateRule.generateRates(overrides, now);

        // 4. Inventory 생성 (날짜별)
        List<Inventory> inventories = createInventories(roomTypeId, command, now);

        return new RateAndInventoryBundle(rateRule, overrides, rates, inventories);
    }

    private List<RateOverride> createOverrides(SetRateAndInventoryCommand command,
                                                RateRule rateRule, Instant now) {
        if (command.overrides() == null || command.overrides().isEmpty()) {
            return List.of();
        }

        return command.overrides().stream()
            .map(item -> RateOverride.forPending(
                rateRule.startDate(),
                rateRule.endDate(),
                item.date(),
                item.price(),
                item.reason(),
                now
            ))
            .toList();
    }

    private List<Inventory> createInventories(RoomTypeId roomTypeId,
                                               SetRateAndInventoryCommand command, Instant now) {
        List<Inventory> inventories = new ArrayList<>();
        for (LocalDate date = command.startDate(); !date.isAfter(command.endDate()); date = date.plusDays(1)) {
            inventories.add(Inventory.forNew(roomTypeId, date, command.baseInventory(), now));
        }
        return inventories;
    }
}

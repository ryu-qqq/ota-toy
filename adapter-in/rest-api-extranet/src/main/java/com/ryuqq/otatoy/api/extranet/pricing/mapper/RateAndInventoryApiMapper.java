package com.ryuqq.otatoy.api.extranet.pricing.mapper;

import com.ryuqq.otatoy.api.extranet.pricing.dto.SetRateAndInventoryApiRequest;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.util.List;

/**
 * 요금/재고 설정 API Request DTO를 Application Command로 변환하는 매퍼.
 * Controller가 Thin Layer를 유지할 수 있도록 변환 로직을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RateAndInventoryApiMapper {

    private RateAndInventoryApiMapper() {}

    /**
     * 요금/재고 설정 API 요청을 Application Command로 변환한다.
     *
     * @param ratePlanId PathVariable에서 받은 요금 정책 ID
     * @param request    요금/재고 설정 요청 DTO
     * @return 요금/재고 설정 Command
     */
    public static SetRateAndInventoryCommand toCommand(Long ratePlanId, SetRateAndInventoryApiRequest request) {
        List<SetRateAndInventoryCommand.OverrideItem> overrides = request.overrides() != null
            ? request.overrides().stream()
                .map(o -> new SetRateAndInventoryCommand.OverrideItem(
                    o.date(),
                    o.price(),
                    o.reason()
                ))
                .toList()
            : List.of();

        return new SetRateAndInventoryCommand(
            RatePlanId.of(ratePlanId),
            request.startDate(),
            request.endDate(),
            request.basePrice(),
            request.weekdayPrice(),
            request.fridayPrice(),
            request.saturdayPrice(),
            request.sundayPrice(),
            request.baseInventory(),
            overrides
        );
    }
}

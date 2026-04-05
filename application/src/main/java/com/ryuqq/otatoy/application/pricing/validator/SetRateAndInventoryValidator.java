package com.ryuqq.otatoy.application.pricing.validator;

import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;

import org.springframework.stereotype.Component;

/**
 * 요금/재고 설정 검증 전용 Validator.
 * RatePlanReadManager를 주입받아 요금 정책 존재 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class SetRateAndInventoryValidator {

    private final RatePlanReadManager ratePlanReadManager;

    public SetRateAndInventoryValidator(RatePlanReadManager ratePlanReadManager) {
        this.ratePlanReadManager = ratePlanReadManager;
    }

    /**
     * 요금/재고 설정 전 RatePlan 존재 여부를 검증한다.
     * 존재하지 않으면 RatePlanNotFoundException 발생 (AC-2).
     */
    public void validate(SetRateAndInventoryCommand command) {
        ratePlanReadManager.verifyExists(command.ratePlanId());
    }
}

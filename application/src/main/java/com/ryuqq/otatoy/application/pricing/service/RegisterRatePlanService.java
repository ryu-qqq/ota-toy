package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.application.pricing.factory.RatePlanFactory;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanCommandManager;
import com.ryuqq.otatoy.application.pricing.port.in.RegisterRatePlanUseCase;
import com.ryuqq.otatoy.application.pricing.validator.RatePlanRegistrationValidator;
import com.ryuqq.otatoy.domain.pricing.RatePlan;

import org.springframework.stereotype.Service;

/**
 * RatePlan 등록 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 Manager에서 관리한다.
 * Port 직접 호출 금지 -- Manager/Factory/Validator만 의존한다 (APP-BC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class RegisterRatePlanService implements RegisterRatePlanUseCase {

    private final RatePlanRegistrationValidator validator;
    private final RatePlanFactory ratePlanFactory;
    private final RatePlanCommandManager ratePlanCommandManager;

    public RegisterRatePlanService(RatePlanRegistrationValidator validator,
                                    RatePlanFactory ratePlanFactory,
                                    RatePlanCommandManager ratePlanCommandManager) {
        this.validator = validator;
        this.ratePlanFactory = ratePlanFactory;
        this.ratePlanCommandManager = ratePlanCommandManager;
    }

    @Override
    public Long execute(RegisterRatePlanCommand command) {
        // 1. 검증 (Validator -- RoomTypeReadManager.verifyExists 경유)
        validator.validate(command);

        // 2. 도메인 객체 생성 (Factory -- TimeProvider, sourceType=DIRECT 고정)
        RatePlan ratePlan = ratePlanFactory.createForDirect(command);

        // 3. 저장 (CommandManager -- @Transactional 메서드 단위)
        return ratePlanCommandManager.persist(ratePlan);
    }
}

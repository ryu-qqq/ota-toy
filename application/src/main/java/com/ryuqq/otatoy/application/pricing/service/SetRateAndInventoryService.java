package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.application.pricing.facade.RateAndInventoryPersistenceFacade;
import com.ryuqq.otatoy.application.pricing.factory.RateAndInventoryFactory;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.port.in.SetRateAndInventoryUseCase;
import com.ryuqq.otatoy.application.pricing.validator.SetRateAndInventoryValidator;
import com.ryuqq.otatoy.domain.pricing.RatePlan;

import org.springframework.stereotype.Service;

/**
 * 요금/재고 설정 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 Manager/PersistenceFacade에서 관리한다.
 * Port 직접 호출 금지 -- Manager/Factory/Validator/Facade만 의존한다 (APP-BC-001, AC-11).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class SetRateAndInventoryService implements SetRateAndInventoryUseCase {

    private final SetRateAndInventoryValidator validator;
    private final RatePlanReadManager ratePlanReadManager;
    private final RateAndInventoryFactory factory;
    private final RateAndInventoryPersistenceFacade persistenceFacade;

    public SetRateAndInventoryService(SetRateAndInventoryValidator validator,
                                       RatePlanReadManager ratePlanReadManager,
                                       RateAndInventoryFactory factory,
                                       RateAndInventoryPersistenceFacade persistenceFacade) {
        this.validator = validator;
        this.ratePlanReadManager = ratePlanReadManager;
        this.factory = factory;
        this.persistenceFacade = persistenceFacade;
    }

    @Override
    public void execute(SetRateAndInventoryCommand command) {
        // 1. RatePlan 존재 확인 (AC-2)
        validator.validate(command);

        // 2. RatePlan 조회 -- roomTypeId 획득용
        RatePlan ratePlan = ratePlanReadManager.getById(command.ratePlanId());

        // 3. 도메인 객체 번들 생성 (Factory -- RateRule + RateOverride + Rate + Inventory)
        //    Rate 생성은 RateRule.generateRates()가 담당 (AC-4, AC-8)
        RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

        // 4. 원자적 저장 (PersistenceFacade -- AC-9)
        persistenceFacade.persist(bundle);
    }
}

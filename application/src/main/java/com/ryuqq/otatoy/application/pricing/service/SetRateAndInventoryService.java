package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.application.pricing.facade.RateAndInventoryPersistenceFacade;
import com.ryuqq.otatoy.application.pricing.factory.RateAndInventoryFactory;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.port.in.SetRateAndInventoryUseCase;
import com.ryuqq.otatoy.application.pricing.validator.SetRateAndInventoryValidator;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.pricing.RatePlan;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final InventoryClientManager inventoryClientManager;

    public SetRateAndInventoryService(SetRateAndInventoryValidator validator,
                                       RatePlanReadManager ratePlanReadManager,
                                       RateAndInventoryFactory factory,
                                       RateAndInventoryPersistenceFacade persistenceFacade,
                                       InventoryClientManager inventoryClientManager) {
        this.validator = validator;
        this.ratePlanReadManager = ratePlanReadManager;
        this.factory = factory;
        this.persistenceFacade = persistenceFacade;
        this.inventoryClientManager = inventoryClientManager;
    }

    @Override
    public void execute(SetRateAndInventoryCommand command) {
        // 1. RatePlan 존재 확인
        validator.validate(command);

        // 2. RatePlan 조회 — roomTypeId 획득용
        RatePlan ratePlan = ratePlanReadManager.getById(command.ratePlanId());

        // 3. 도메인 객체 번들 생성
        RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

        // 4. DB 원자적 저장
        persistenceFacade.persist(bundle);

        // 5. Redis 재고 카운터 초기화
        Map<LocalDate, Integer> dateStockMap = bundle.inventories().stream()
            .collect(Collectors.toMap(Inventory::inventoryDate, Inventory::totalInventory));
        inventoryClientManager.initializeStock(ratePlan.roomTypeId(), dateStockMap);
    }
}

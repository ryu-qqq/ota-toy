package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.pricing.assembler.PropertyRateAssembler;
import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.application.pricing.factory.RateCriteriaFactory;
import com.ryuqq.otatoy.application.pricing.manager.RateCacheManager;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.port.in.CustomerGetRateUseCase;
import com.ryuqq.otatoy.application.inventory.manager.InventoryReadManager;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.inventory.Inventories;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 고객 요금 조회 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 — 트랜잭션 경계는 Manager에서 관리한다.
 *
 * 조회 흐름:
 * 1. Query → Criteria 변환 (Factory)
 * 2. 객실 유형 조회 + 인원 필터 (RoomTypeReadManager)
 * 3. 요금 정책 조회 (RatePlanReadManager)
 * 4. 날짜별 요금 조회 — Redis 캐시 우선 (RateCacheManager)
 * 5. 재고 조회 (InventoryReadManager)
 * 6. 결과 조립 (PropertyRateAssembler)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CustomerGetRateService implements CustomerGetRateUseCase {

    private final RateCriteriaFactory criteriaFactory;
    private final RoomTypeReadManager roomTypeReadManager;
    private final RatePlanReadManager ratePlanReadManager;
    private final RateCacheManager rateCacheManager;
    private final InventoryReadManager inventoryReadManager;
    private final PropertyRateAssembler assembler;
    private final MeterRegistry meterRegistry;

    public CustomerGetRateService(RateCriteriaFactory criteriaFactory,
                                   RoomTypeReadManager roomTypeReadManager,
                                   RatePlanReadManager ratePlanReadManager,
                                   RateCacheManager rateCacheManager,
                                   InventoryReadManager inventoryReadManager,
                                   PropertyRateAssembler assembler,
                                   MeterRegistry meterRegistry) {
        this.criteriaFactory = criteriaFactory;
        this.roomTypeReadManager = roomTypeReadManager;
        this.ratePlanReadManager = ratePlanReadManager;
        this.rateCacheManager = rateCacheManager;
        this.inventoryReadManager = inventoryReadManager;
        this.assembler = assembler;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public CustomerPropertyRateResult execute(CustomerGetRateQuery query) {
        Timer.Sample sample = Timer.start(meterRegistry);

        RateFetchCriteria criteria = criteriaFactory.create(query);

        // 1. 수용 가능한 객실 유형 조회
        RoomTypes roomTypes = roomTypeReadManager.findActiveByPropertyIdAndMinOccupancy(
                criteria.propertyId(), criteria.guests());

        if (roomTypes.isEmpty()) {
            sample.stop(meterRegistry.timer("rate.query.duration"));
            return CustomerPropertyRateResult.empty(criteria.propertyId());
        }

        // 2. 객실별 요금 정책 조회
        RatePlans ratePlans = ratePlanReadManager.findByRoomTypeIds(roomTypes.roomTypeIds());

        if (ratePlans.isEmpty()) {
            sample.stop(meterRegistry.timer("rate.query.duration"));
            return CustomerPropertyRateResult.empty(criteria.propertyId());
        }

        // 3. 날짜별 요금 조회 — Redis 캐시 경유
        Map<String, BigDecimal> rateCache = rateCacheManager.getRates(
                ratePlans.ratePlanIds(), criteria.stayDates());

        // 4. 재고 조회
        Inventories inventories = inventoryReadManager.findByRoomTypeIdsAndDateRange(
                roomTypes.roomTypeIds(), criteria.checkIn(), criteria.checkOut());

        // 5. 결과 조립
        CustomerPropertyRateResult result = assembler.toResult(criteria, roomTypes, ratePlans, rateCache, inventories);
        sample.stop(meterRegistry.timer("rate.query.duration"));
        return result;
    }
}

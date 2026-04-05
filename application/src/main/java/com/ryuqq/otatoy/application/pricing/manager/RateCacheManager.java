package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RateCachePort;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rate 캐시 관리 ClientManager.
 * Redis 캐시를 먼저 일괄 조회하고, 미스분만 DB에서 읽어 캐시에 적재한다.
 * 트랜잭션 없음 — 외부 시스템(Redis) 호출이므로 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateCacheManager {

    private final RateCachePort rateCachePort;
    private final RateReadManager rateReadManager;

    public RateCacheManager(RateCachePort rateCachePort, RateReadManager rateReadManager) {
        this.rateCachePort = rateCachePort;
        this.rateReadManager = rateReadManager;
    }

    /**
     * 여러 요금 정책의 날짜별 요금을 일괄 조회한다.
     * Redis MGET 1회 → 미스분만 DB 조회 → Redis MSET 1회.
     *
     * @param ratePlanIds 요금 정책 ID 목록
     * @param dates 조회 날짜 목록
     * @return "ratePlanId:date" → price 매핑
     */
    public Map<String, BigDecimal> getRates(List<RatePlanId> ratePlanIds, List<LocalDate> dates) {
        List<Long> rawIds = ratePlanIds.stream().map(RatePlanId::value).toList();

        // 1. Redis 일괄 조회 (MGET 1회)
        Map<String, BigDecimal> cached = rateCachePort.multiGet(rawIds, dates);

        // 2. 미스 키 식별
        Map<RatePlanId, List<LocalDate>> missedByPlan = findMissed(ratePlanIds, dates, cached);

        if (!missedByPlan.isEmpty()) {
            // 3. DB 일괄 조회 + Redis 일괄 적재 (MSET 1회)
            Map<String, BigDecimal> fromDb = loadMissedFromDb(missedByPlan);
            if (!fromDb.isEmpty()) {
                rateCachePort.multiSet(fromDb);
                cached.putAll(fromDb);
            }
        }

        return cached;
    }

    private Map<RatePlanId, List<LocalDate>> findMissed(List<RatePlanId> ratePlanIds,
                                                         List<LocalDate> dates,
                                                         Map<String, BigDecimal> cached) {
        Map<RatePlanId, List<LocalDate>> missed = new HashMap<>();
        for (RatePlanId ratePlanId : ratePlanIds) {
            List<LocalDate> missedDates = dates.stream()
                    .filter(date -> !cached.containsKey(cacheKey(ratePlanId.value(), date)))
                    .toList();
            if (!missedDates.isEmpty()) {
                missed.put(ratePlanId, missedDates);
            }
        }
        return missed;
    }

    private Map<String, BigDecimal> loadMissedFromDb(Map<RatePlanId, List<LocalDate>> missedByPlan) {
        List<RatePlanId> missedPlanIds = missedByPlan.keySet().stream().toList();
        Set<LocalDate> allMissedDates = missedByPlan.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        LocalDate minDate = allMissedDates.stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate maxDate = allMissedDates.stream().max(LocalDate::compareTo).orElseThrow().plusDays(1);

        List<Rate> rates = rateReadManager.findByRatePlanIdsAndDateRange(missedPlanIds, minDate, maxDate);

        return rates.stream()
                .filter(r -> allMissedDates.contains(r.rateDate()))
                .collect(Collectors.toMap(
                        r -> cacheKey(r.ratePlanId().value(), r.rateDate()),
                        Rate::basePrice,
                        (a, b) -> a
                ));
    }

    private static String cacheKey(Long ratePlanId, LocalDate date) {
        return ratePlanId + ":" + date;
    }
}

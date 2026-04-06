package com.ryuqq.otatoy.application.supplier.strategy;

import com.ryuqq.otatoy.domain.supplier.SupplierApiType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 공급자 전략 제공자.
 * DI로 주입받은 모든 SupplierStrategy 구현체를 apiType 기준 Map으로 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierStrategyProvider {

    private final Map<SupplierApiType, SupplierStrategy> strategyMap;

    public SupplierStrategyProvider(List<SupplierStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(SupplierStrategy::getApiType, s -> s));
    }

    /**
     * apiType에 해당하는 전략을 반환한다.
     * 지원하지 않는 유형이면 예외를 던진다.
     */
    public SupplierStrategy getStrategy(SupplierApiType apiType) {
        return Optional.ofNullable(strategyMap.get(apiType))
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 공급자 API 유형입니다: " + apiType));
    }
}

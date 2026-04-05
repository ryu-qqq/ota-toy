package com.ryuqq.otatoy.domain.pricing;

import java.util.List;

/**
 * 요금 규칙 일급 컬렉션.
 * 동일 RatePlan에 속하는 RateRule들의 기간 겹침을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class RateRules {

    private final List<RateRule> items;

    private RateRules(List<RateRule> items) {
        this.items = items;
    }

    public static RateRules forNew(List<RateRule> items) {
        if (items == null || items.isEmpty()) {
            return new RateRules(List.of());
        }
        validateNoOverlap(items);
        return new RateRules(List.copyOf(items));
    }

    public static RateRules reconstitute(List<RateRule> items) {
        if (items == null || items.isEmpty()) {
            return new RateRules(List.of());
        }
        return new RateRules(List.copyOf(items));
    }

    private static void validateNoOverlap(List<RateRule> items) {
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                RateRule a = items.get(i);
                RateRule b = items.get(j);
                if (!a.startDate().isAfter(b.endDate()) && !b.startDate().isAfter(a.endDate())) {
                    throw new IllegalArgumentException(
                            "요금 규칙 기간이 겹칩니다: ["
                                    + a.startDate() + "~" + a.endDate() + "] vs ["
                                    + b.startDate() + "~" + b.endDate() + "]"
                    );
                }
            }
        }
    }

    public List<RateRule> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

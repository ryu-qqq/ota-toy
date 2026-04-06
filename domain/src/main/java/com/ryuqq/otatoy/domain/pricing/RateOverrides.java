package com.ryuqq.otatoy.domain.pricing;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/**
 * 요금 오버라이드 일급 컬렉션.
 * 동일 RateRule에 속하는 RateOverride들의 날짜 중복을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class RateOverrides {

    private final List<RateOverride> items;

    private RateOverrides(List<RateOverride> items) {
        this.items = items;
    }

    public static RateOverrides forNew(List<RateOverride> items) {
        if (items == null || items.isEmpty()) {
            return new RateOverrides(List.of());
        }
        validateNoDuplicateDate(items);
        return new RateOverrides(List.copyOf(items));
    }

    public static RateOverrides reconstitute(List<RateOverride> items) {
        if (items == null || items.isEmpty()) {
            return new RateOverrides(List.of());
        }
        return new RateOverrides(List.copyOf(items));
    }

    private static void validateNoDuplicateDate(List<RateOverride> items) {
        long distinctCount = items.stream()
                .map(RateOverride::overrideDate)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            List<LocalDate> duplicates = items.stream()
                    .map(RateOverride::overrideDate)
                    .filter(date -> items.stream()
                            .map(RateOverride::overrideDate)
                            .filter(date::equals)
                            .count() > 1)
                    .distinct()
                    .toList();
            throw new IllegalArgumentException("오버라이드 날짜가 중복됩니다: " + duplicates);
        }
    }

    public Stream<RateOverride> stream() {
        return items.stream();
    }

    public List<RateOverride> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

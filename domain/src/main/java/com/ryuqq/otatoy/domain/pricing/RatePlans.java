package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 요금 정책 일급 컬렉션.
 * ID 추출, RoomType별 그룹핑 등 컬렉션 연산을 캡슐화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class RatePlans {

    private final List<RatePlan> items;

    private RatePlans(List<RatePlan> items) {
        this.items = items;
    }

    public static RatePlans from(List<RatePlan> items) {
        if (items == null || items.isEmpty()) {
            return new RatePlans(List.of());
        }
        return new RatePlans(List.copyOf(items));
    }

    public List<RatePlanId> ratePlanIds() {
        return items.stream()
                .map(RatePlan::id)
                .toList();
    }

    public Map<RoomTypeId, List<RatePlan>> groupByRoomTypeId() {
        return items.stream()
                .collect(Collectors.groupingBy(RatePlan::roomTypeId));
    }

    public Stream<RatePlan> stream() {
        return items.stream();
    }

    public List<RatePlan> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 재고 일급 컬렉션.
 * RoomType별·날짜별 그룹핑 등 컬렉션 연산을 캡슐화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class Inventories {

    private final List<Inventory> items;

    private Inventories(List<Inventory> items) {
        this.items = items;
    }

    public static Inventories from(List<Inventory> items) {
        if (items == null || items.isEmpty()) {
            return new Inventories(List.of());
        }
        return new Inventories(List.copyOf(items));
    }

    public Map<RoomTypeId, Map<LocalDate, Inventory>> groupByRoomTypeAndDate() {
        return items.stream()
                .collect(Collectors.groupingBy(
                        Inventory::roomTypeId,
                        Collectors.toMap(Inventory::inventoryDate, inv -> inv, (a, b) -> a)
                ));
    }

    public Stream<Inventory> stream() {
        return items.stream();
    }

    public List<Inventory> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

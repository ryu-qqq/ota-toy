package com.ryuqq.otatoy.domain.roomtype;

import java.util.List;

/**
 * 객실 침대 구성 일급 컬렉션.
 * 침대 유형 중복 검증을 수행한다.
 */
public class RoomTypeBeds {

    private final List<RoomTypeBed> items;

    private RoomTypeBeds(List<RoomTypeBed> items) {
        this.items = items;
    }

    public static RoomTypeBeds forNew(List<RoomTypeBed> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeBeds(List.of());
        }
        validateNoDuplicateBedType(items);
        return new RoomTypeBeds(List.copyOf(items));
    }

    public static RoomTypeBeds reconstitute(List<RoomTypeBed> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeBeds(List.of());
        }
        return new RoomTypeBeds(List.copyOf(items));
    }

    private static void validateNoDuplicateBedType(List<RoomTypeBed> items) {
        long distinctCount = items.stream()
                .map(RoomTypeBed::bedTypeId)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("객실 침대 유형이 중복됩니다");
        }
    }

    public int totalQuantity() {
        return items.stream()
                .mapToInt(RoomTypeBed::quantity)
                .sum();
    }

    public List<RoomTypeBed> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

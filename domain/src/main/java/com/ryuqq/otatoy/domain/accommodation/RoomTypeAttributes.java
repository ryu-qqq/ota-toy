package com.ryuqq.otatoy.domain.accommodation;

import java.util.List;

public class RoomTypeAttributes {

    private final List<RoomTypeAttribute> items;

    private RoomTypeAttributes(List<RoomTypeAttribute> items) {
        this.items = items;
    }

    public static RoomTypeAttributes forNew(List<RoomTypeAttribute> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeAttributes(List.of());
        }
        validateNoDuplicateKey(items);
        return new RoomTypeAttributes(List.copyOf(items));
    }

    public static RoomTypeAttributes reconstitute(List<RoomTypeAttribute> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeAttributes(List.of());
        }
        return new RoomTypeAttributes(List.copyOf(items));
    }

    private static void validateNoDuplicateKey(List<RoomTypeAttribute> items) {
        long distinctCount = items.stream()
                .map(RoomTypeAttribute::attributeKey)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("객실 속성 키가 중복됩니다");
        }
    }

    public List<RoomTypeAttribute> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

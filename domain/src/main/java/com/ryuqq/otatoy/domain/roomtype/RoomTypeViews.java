package com.ryuqq.otatoy.domain.roomtype;

import java.util.List;

/**
 * 객실 전망 매핑 일급 컬렉션.
 * 전망 유형 중복 검증을 수행한다.
 */
public class RoomTypeViews {

    private final List<RoomTypeView> items;

    private RoomTypeViews(List<RoomTypeView> items) {
        this.items = items;
    }

    public static RoomTypeViews forNew(List<RoomTypeView> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeViews(List.of());
        }
        validateNoDuplicateViewType(items);
        return new RoomTypeViews(List.copyOf(items));
    }

    public static RoomTypeViews reconstitute(List<RoomTypeView> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypeViews(List.of());
        }
        return new RoomTypeViews(List.copyOf(items));
    }

    private static void validateNoDuplicateViewType(List<RoomTypeView> items) {
        long distinctCount = items.stream()
                .map(RoomTypeView::viewTypeId)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("객실 전망 유형이 중복됩니다");
        }
    }

    public List<RoomTypeView> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

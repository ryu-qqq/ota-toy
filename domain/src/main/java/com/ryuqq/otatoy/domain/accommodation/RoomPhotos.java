package com.ryuqq.otatoy.domain.accommodation;

import java.util.List;

public class RoomPhotos {

    private final List<RoomPhoto> items;

    private RoomPhotos(List<RoomPhoto> items) {
        this.items = items;
    }

    public static RoomPhotos forNew(List<RoomPhoto> items) {
        if (items == null || items.isEmpty()) {
            return new RoomPhotos(List.of());
        }
        validateNoDuplicateSortOrder(items);
        return new RoomPhotos(List.copyOf(items));
    }

    public static RoomPhotos reconstitute(List<RoomPhoto> items) {
        if (items == null || items.isEmpty()) {
            return new RoomPhotos(List.of());
        }
        return new RoomPhotos(List.copyOf(items));
    }

    private static void validateNoDuplicateSortOrder(List<RoomPhoto> items) {
        long distinctCount = items.stream()
                .mapToInt(RoomPhoto::sortOrder)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("객실 사진의 정렬 순서가 중복됩니다");
        }
    }

    public List<RoomPhoto> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

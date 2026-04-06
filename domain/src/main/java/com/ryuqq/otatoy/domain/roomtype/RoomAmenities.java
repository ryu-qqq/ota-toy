package com.ryuqq.otatoy.domain.roomtype;

import java.util.List;
import java.util.stream.Stream;

/**
 * 객실 편의시설 일급 컬렉션.
 * 정렬 순서 중복 검증을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class RoomAmenities {

    private final List<RoomAmenity> items;

    private RoomAmenities(List<RoomAmenity> items) {
        this.items = items;
    }

    public static RoomAmenities forNew(List<RoomAmenity> items) {
        if (items == null || items.isEmpty()) {
            return new RoomAmenities(List.of());
        }
        validateNoDuplicateSortOrder(items);
        return new RoomAmenities(List.copyOf(items));
    }

    public static RoomAmenities reconstitute(List<RoomAmenity> items) {
        if (items == null || items.isEmpty()) {
            return new RoomAmenities(List.of());
        }
        return new RoomAmenities(List.copyOf(items));
    }

    private static void validateNoDuplicateSortOrder(List<RoomAmenity> items) {
        long distinctCount = items.stream()
                .mapToInt(RoomAmenity::sortOrder)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("객실 편의시설의 정렬 순서가 중복됩니다");
        }
    }

    public Stream<RoomAmenity> stream() {
        return items.stream();
    }

    public List<RoomAmenity> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

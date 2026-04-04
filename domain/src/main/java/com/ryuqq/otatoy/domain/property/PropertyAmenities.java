package com.ryuqq.otatoy.domain.property;

import java.util.List;

/**
 * 숙소 편의시설 일급 컬렉션.
 * 정렬 순서 중복 검증을 수행한다.
 */
public class PropertyAmenities {

    private final List<PropertyAmenity> items;

    private PropertyAmenities(List<PropertyAmenity> items) {
        this.items = items;
    }

    public static PropertyAmenities forNew(List<PropertyAmenity> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyAmenities(List.of());
        }
        validateNoDuplicateSortOrder(items);
        return new PropertyAmenities(List.copyOf(items));
    }

    public static PropertyAmenities reconstitute(List<PropertyAmenity> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyAmenities(List.of());
        }
        return new PropertyAmenities(List.copyOf(items));
    }

    private static void validateNoDuplicateSortOrder(List<PropertyAmenity> items) {
        long distinctCount = items.stream()
                .mapToInt(PropertyAmenity::sortOrder)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("숙소 편의시설의 정렬 순서가 중복됩니다");
        }
    }

    public List<PropertyAmenity> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

package com.ryuqq.otatoy.domain.property;

import java.util.List;

/**
 * 숙소 사진 일급 컬렉션.
 * 정렬 순서 중복 검증을 수행한다.
 */
public class PropertyPhotos {

    private final List<PropertyPhoto> items;

    private PropertyPhotos(List<PropertyPhoto> items) {
        this.items = items;
    }

    public static PropertyPhotos forNew(List<PropertyPhoto> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyPhotos(List.of());
        }
        validateNoDuplicateSortOrder(items);
        return new PropertyPhotos(List.copyOf(items));
    }

    public static PropertyPhotos reconstitute(List<PropertyPhoto> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyPhotos(List.of());
        }
        return new PropertyPhotos(List.copyOf(items));
    }

    private static void validateNoDuplicateSortOrder(List<PropertyPhoto> items) {
        long distinctCount = items.stream()
                .mapToInt(PropertyPhoto::sortOrder)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("숙소 사진의 정렬 순서가 중복됩니다");
        }
    }

    public List<PropertyPhoto> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

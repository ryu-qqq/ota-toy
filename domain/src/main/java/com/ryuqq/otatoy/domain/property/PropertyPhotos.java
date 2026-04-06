package com.ryuqq.otatoy.domain.property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 숙소 사진 일급 컬렉션.
 * 정렬 순서 중복 검증을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
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

    /**
     * 기존 사진과 새 사진을 비교하여 diff를 계산한다.
     * photoKey(originUrl + photoType)로 동일 사진을 식별한다.
     */
    public PropertyPhotoDiff update(PropertyPhotos newPhotos) {
        if (newPhotos.isEmpty() && items.isEmpty()) {
            return new PropertyPhotoDiff(List.of(), List.of(), List.of(), null);
        }

        Instant now = newPhotos.isEmpty()
                ? items.getFirst().createdAt()
                : newPhotos.items().getFirst().createdAt();

        Map<String, PropertyPhoto> existingByKey = items.stream()
                .collect(Collectors.toMap(PropertyPhoto::photoKey, p -> p));

        Set<String> newKeys = newPhotos.stream()
                .map(PropertyPhoto::photoKey)
                .collect(Collectors.toSet());

        List<PropertyPhoto> added = new ArrayList<>();
        List<PropertyPhoto> retained = new ArrayList<>();

        for (PropertyPhoto newPhoto : newPhotos.items()) {
            String key = newPhoto.photoKey();
            if (existingByKey.containsKey(key)) {
                retained.add(existingByKey.get(key));
            } else {
                added.add(newPhoto);
            }
        }

        List<PropertyPhoto> removed = new ArrayList<>();
        for (PropertyPhoto existing : items) {
            if (!newKeys.contains(existing.photoKey())) {
                existing.delete(now);
                removed.add(existing);
            }
        }

        return new PropertyPhotoDiff(added, removed, retained, now);
    }

    public Stream<PropertyPhoto> stream() {
        return items.stream();
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

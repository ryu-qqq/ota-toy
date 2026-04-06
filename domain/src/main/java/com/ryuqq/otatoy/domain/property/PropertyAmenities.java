package com.ryuqq.otatoy.domain.property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 숙소 편의시설 일급 컬렉션.
 * 정렬 순서 중복 검증을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
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

    /**
     * 기존 편의시설과 새 편의시설을 비교하여 diff를 계산한다.
     * amenityType + name 조합(amenityKey)으로 동일 편의시설을 식별한다.
     * - 새 목록에 있고 기존에도 있으면 → retained
     * - 새 목록에 있고 기존에 없으면 → added
     * - 기존에 있고 새 목록에 없으면 → removed (soft delete 처리)
     */
    /**
     * 기존 편의시설과 새 편의시설을 비교하여 diff를 계산한다.
     * amenityKey()로 동일 편의시설을 식별한다.
     * occurredAt은 새 편의시설의 createdAt에서 추출한다.
     */
    public PropertyAmenityDiff update(PropertyAmenities newAmenities) {
        if (newAmenities.isEmpty() && items.isEmpty()) {
            return new PropertyAmenityDiff(List.of(), List.of(), List.of(), null);
        }

        Instant now = newAmenities.isEmpty()
                ? items.getFirst().createdAt()
                : newAmenities.items().getFirst().createdAt();

        Map<String, PropertyAmenity> existingByKey = items.stream()
                .collect(Collectors.toMap(PropertyAmenity::amenityKey, a -> a));

        Set<String> newKeys = newAmenities.stream()
                .map(PropertyAmenity::amenityKey)
                .collect(Collectors.toSet());

        List<PropertyAmenity> added = new ArrayList<>();
        List<PropertyAmenity> retained = new ArrayList<>();

        for (PropertyAmenity newAmenity : newAmenities.items()) {
            String key = newAmenity.amenityKey();
            if (existingByKey.containsKey(key)) {
                retained.add(existingByKey.get(key));
            } else {
                added.add(newAmenity);
            }
        }

        List<PropertyAmenity> removed = new ArrayList<>();
        for (PropertyAmenity existing : items) {
            if (!newKeys.contains(existing.amenityKey())) {
                existing.delete(now);
                removed.add(existing);
            }
        }

        return new PropertyAmenityDiff(added, removed, retained, now);
    }

    public Stream<PropertyAmenity> stream() {
        return items.stream();
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

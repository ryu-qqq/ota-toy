package com.ryuqq.otatoy.domain.property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 숙소 속성값 일급 컬렉션.
 * 동일 속성 중복 검증을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PropertyAttributeValues {

    private final List<PropertyAttributeValue> items;

    private PropertyAttributeValues(List<PropertyAttributeValue> items) {
        this.items = items;
    }

    public static PropertyAttributeValues forNew(List<PropertyAttributeValue> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyAttributeValues(List.of());
        }
        validateNoDuplicateAttribute(items);
        return new PropertyAttributeValues(List.copyOf(items));
    }

    public static PropertyAttributeValues reconstitute(List<PropertyAttributeValue> items) {
        if (items == null || items.isEmpty()) {
            return new PropertyAttributeValues(List.of());
        }
        return new PropertyAttributeValues(List.copyOf(items));
    }

    private static void validateNoDuplicateAttribute(List<PropertyAttributeValue> items) {
        long distinctCount = items.stream()
                .map(PropertyAttributeValue::propertyTypeAttributeId)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("숙소 속성값의 속성 ID가 중복됩니다");
        }
    }

    /**
     * 기존 속성값과 새 속성값을 비교하여 diff를 계산한다.
     * propertyTypeAttributeId(attributeKey)로 동일 속성을 식별한다.
     * - 새 목록에 있고 기존에도 있으면 → retained
     * - 새 목록에 있고 기존에 없으면 → added
     * - 기존에 있고 새 목록에 없으면 → removed (soft delete 처리)
     */
    /**
     * 기존 속성값과 새 속성값을 비교하여 diff를 계산한다.
     * attributeKey()로 동일 속성을 식별한다.
     * occurredAt은 새 속성값의 createdAt에서 추출한다.
     */
    public PropertyAttributeValueDiff update(PropertyAttributeValues newValues) {
        if (newValues.isEmpty() && items.isEmpty()) {
            return new PropertyAttributeValueDiff(List.of(), List.of(), List.of(), null);
        }

        Instant now = newValues.isEmpty()
                ? items.getFirst().createdAt()
                : newValues.items().getFirst().createdAt();

        Map<Long, PropertyAttributeValue> existingByKey = items.stream()
                .collect(Collectors.toMap(PropertyAttributeValue::attributeKey, v -> v));

        Set<Long> newKeys = newValues.stream()
                .map(PropertyAttributeValue::attributeKey)
                .collect(Collectors.toSet());

        List<PropertyAttributeValue> added = new ArrayList<>();
        List<PropertyAttributeValue> retained = new ArrayList<>();

        for (PropertyAttributeValue newValue : newValues.items()) {
            Long key = newValue.attributeKey();
            if (existingByKey.containsKey(key)) {
                retained.add(existingByKey.get(key));
            } else {
                added.add(newValue);
            }
        }

        List<PropertyAttributeValue> removed = new ArrayList<>();
        for (PropertyAttributeValue existing : items) {
            if (!newKeys.contains(existing.attributeKey())) {
                existing.delete(now);
                removed.add(existing);
            }
        }

        return new PropertyAttributeValueDiff(added, removed, retained, now);
    }

    public Stream<PropertyAttributeValue> stream() {
        return items.stream();
    }

    public List<PropertyAttributeValue> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

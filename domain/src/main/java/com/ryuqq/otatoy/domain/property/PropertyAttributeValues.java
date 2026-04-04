package com.ryuqq.otatoy.domain.property;

import java.util.List;

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

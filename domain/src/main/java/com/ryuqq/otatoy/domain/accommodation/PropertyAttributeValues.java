package com.ryuqq.otatoy.domain.accommodation;

import java.util.List;

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

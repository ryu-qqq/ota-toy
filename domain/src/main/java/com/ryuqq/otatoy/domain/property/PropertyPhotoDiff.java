package com.ryuqq.otatoy.domain.property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 숙소 사진 diff 결과.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertyPhotoDiff(
    List<PropertyPhoto> added,
    List<PropertyPhoto> removed,
    List<PropertyPhoto> retained,
    Instant occurredAt
) {

    public boolean hasNoChanges() {
        return added.isEmpty() && removed.isEmpty();
    }

    public List<PropertyPhoto> allPersistTargets() {
        List<PropertyPhoto> result = new ArrayList<>(added.size() + retained.size() + removed.size());
        result.addAll(added);
        result.addAll(retained);
        result.addAll(removed);
        return result;
    }
}

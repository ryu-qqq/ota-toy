package com.ryuqq.otatoy.domain.property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 속성값 diff 결과. 기존 목록과 신규 목록을 비교하여
 * 추가(added), 삭제(removed), 유지(retained)로 분류한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record PropertyAttributeValueDiff(
    List<PropertyAttributeValue> added,
    List<PropertyAttributeValue> removed,
    List<PropertyAttributeValue> retained,
    Instant occurredAt
) {

    /**
     * 변경 사항이 없는지 확인한다.
     */
    public boolean hasNoChanges() {
        return added.isEmpty() && removed.isEmpty();
    }

    /**
     * persist 대상인 모든 dirty 엔티티를 반환한다.
     * added(INSERT) + removed(UPDATE — soft delete 상태) + retained(변경 없음이지만 안전하게 포함).
     */
    public List<PropertyAttributeValue> allPersistTargets() {
        List<PropertyAttributeValue> result = new ArrayList<>(added.size() + removed.size() + retained.size());
        result.addAll(added);
        result.addAll(removed);
        result.addAll(retained);
        return result;
    }
}

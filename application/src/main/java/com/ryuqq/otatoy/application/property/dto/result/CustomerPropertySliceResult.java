package com.ryuqq.otatoy.application.property.dto.result;

import com.ryuqq.otatoy.domain.common.query.SliceMeta;

import java.util.List;

/**
 * 고객 숙소 검색 전용 결과.
 * 제네릭 SliceResult 대신 전용 Result로 래핑하여 확장성을 확보한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CustomerPropertySliceResult(
        List<PropertySummary> content,
        SliceMeta sliceMeta
) {

    public CustomerPropertySliceResult {
        if (content == null) {
            content = List.of();
        }
        if (sliceMeta == null) {
            sliceMeta = new SliceMeta(false, null);
        }
    }

    public static CustomerPropertySliceResult of(List<PropertySummary> content, SliceMeta sliceMeta) {
        return new CustomerPropertySliceResult(content, sliceMeta);
    }

    public static CustomerPropertySliceResult empty() {
        return new CustomerPropertySliceResult(List.of(), new SliceMeta(false, null));
    }

    public boolean hasNext() {
        return sliceMeta.hasNext();
    }

    public Long nextCursor() {
        return sliceMeta.nextCursor();
    }
}

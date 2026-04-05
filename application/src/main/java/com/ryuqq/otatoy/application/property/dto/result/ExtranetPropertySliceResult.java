package com.ryuqq.otatoy.application.property.dto.result;

import com.ryuqq.otatoy.domain.common.query.SliceMeta;

import java.util.List;

/**
 * 파트너 숙소 목록 조회 전용 결과.
 * 제네릭 SliceResult 대신 전용 Result로 래핑하여 확장성을 확보한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExtranetPropertySliceResult(
        List<PropertySummary> content,
        SliceMeta sliceMeta
) {

    public ExtranetPropertySliceResult {
        if (content == null) {
            content = List.of();
        }
        if (sliceMeta == null) {
            sliceMeta = new SliceMeta(false, null);
        }
    }

    public static ExtranetPropertySliceResult of(List<PropertySummary> content, SliceMeta sliceMeta) {
        return new ExtranetPropertySliceResult(content, sliceMeta);
    }

    public static ExtranetPropertySliceResult empty() {
        return new ExtranetPropertySliceResult(List.of(), new SliceMeta(false, null));
    }

    public boolean hasNext() {
        return sliceMeta.hasNext();
    }

    public Long nextCursor() {
        return sliceMeta.nextCursor();
    }
}

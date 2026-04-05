package com.ryuqq.otatoy.application.common.dto;

import com.ryuqq.otatoy.domain.common.query.SliceMeta;

import java.util.List;

/**
 * 커서 기반 페이지네이션 결과 래퍼.
 * 검색 결과 목록 + 다음 페이지 존재 여부 + 다음 커서를 포함한다.
 *
 * @param <T> 결과 항목 타입
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SliceResult<T>(List<T> content, SliceMeta sliceMeta) {

    public SliceResult {
        if (content == null) {
            content = List.of();
        }
        if (sliceMeta == null) {
            sliceMeta = new SliceMeta(false, null);
        }
    }

    public static <T> SliceResult<T> of(List<T> content, SliceMeta sliceMeta) {
        return new SliceResult<>(content, sliceMeta);
    }

    public static <T> SliceResult<T> empty() {
        return new SliceResult<>(List.of(), new SliceMeta(false, null));
    }

    public boolean hasNext() {
        return sliceMeta.hasNext();
    }

    public Long nextCursor() {
        return sliceMeta.nextCursor();
    }
}

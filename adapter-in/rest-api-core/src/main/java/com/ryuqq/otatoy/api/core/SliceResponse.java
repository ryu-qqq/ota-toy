package com.ryuqq.otatoy.api.core;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답 래퍼.
 * List를 직접 반환하지 않고, 다음 페이지 존재 여부와 커서를 함께 전달한다.
 *
 * @param <T> 목록 항목 타입
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SliceResponse<T>(
    List<T> content,
    boolean hasNext,
    Long nextCursor
) {

    /**
     * SliceResponse를 생성한다.
     */
    public static <T> SliceResponse<T> of(List<T> content, boolean hasNext, Long nextCursor) {
        return new SliceResponse<>(content, hasNext, nextCursor);
    }
}

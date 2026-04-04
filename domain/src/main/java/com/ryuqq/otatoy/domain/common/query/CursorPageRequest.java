package com.ryuqq.otatoy.domain.common.query;

/**
 * 커서 기반 페이지네이션 요청. size는 1~100 범위.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record CursorPageRequest<C>(C cursor, int size) {

    public CursorPageRequest {
        if (size < 1 || size > 100) throw new IllegalArgumentException("size는 1~100이어야 합니다");
    }
}

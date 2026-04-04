package com.ryuqq.otatoy.domain.common.query;

/**
 * 오프셋 기반 페이지네이션 요청. page는 0 이상, size는 1~100 범위.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PageRequest(int page, int size) {

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size는 1~100이어야 합니다");
    }

    public long offset() {
        return (long) page * size;
    }
}

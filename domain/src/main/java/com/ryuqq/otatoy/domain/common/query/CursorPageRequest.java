package com.ryuqq.otatoy.domain.common.query;

public record CursorPageRequest<C>(C cursor, int size) {

    public CursorPageRequest {
        if (size < 1 || size > 100) throw new IllegalArgumentException("size는 1~100이어야 합니다");
    }
}

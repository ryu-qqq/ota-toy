package com.ryuqq.otatoy.domain.common.query;

import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.sort.SortKey;

/**
 * 정렬 키, 방향, 페이지 크기, 커서를 포함하는 범용 쿼리 컨텍스트.
 */
public record QueryContext<K extends SortKey>(
        K sortKey,
        SortDirection direction,
        int size,
        Long cursor
) {
}

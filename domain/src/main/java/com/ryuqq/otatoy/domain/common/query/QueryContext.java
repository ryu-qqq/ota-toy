package com.ryuqq.otatoy.domain.common.query;

import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.sort.SortKey;

public record QueryContext<K extends SortKey>(
        K sortKey,
        SortDirection direction,
        int size,
        Long cursor
) {
}

package com.ryuqq.otatoy.application.property.dto.query;

import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.property.PropertySortKey;

import java.time.LocalDate;
import java.util.List;

/**
 * SearchPropertyQuery 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class SearchPropertyQueryFixture {

    private SearchPropertyQueryFixture() {}

    public static final LocalDate DEFAULT_CHECK_IN = LocalDate.of(2026, 4, 10);
    public static final LocalDate DEFAULT_CHECK_OUT = LocalDate.of(2026, 4, 12);
    public static final int DEFAULT_GUESTS = 2;
    public static final int DEFAULT_SIZE = 10;

    /**
     * 기본 검색 쿼리 (서울, 2박, 2명)
     */
    public static SearchPropertyQuery aSearchPropertyQuery() {
        return new SearchPropertyQuery(
                null, "서울", null,
                DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_GUESTS,
                null, null, List.of(),
                false, null,
                PropertySortKey.PRICE_LOW, SortDirection.ASC,
                DEFAULT_SIZE, null
        );
    }

    /**
     * 커서가 있는 검색 쿼리
     */
    public static SearchPropertyQuery aSearchPropertyQueryWithCursor(Long cursor) {
        return new SearchPropertyQuery(
                null, "서울", null,
                DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_GUESTS,
                null, null, List.of(),
                false, null,
                PropertySortKey.PRICE_LOW, SortDirection.ASC,
                DEFAULT_SIZE, cursor
        );
    }
}

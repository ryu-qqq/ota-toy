package com.ryuqq.otatoy.application.pricing.dto.query;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.time.LocalDate;

/**
 * FetchRateQuery 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class FetchRateQueryFixture {

    private FetchRateQueryFixture() {}

    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final LocalDate DEFAULT_CHECK_IN = LocalDate.of(2026, 4, 10);
    public static final LocalDate DEFAULT_CHECK_OUT = LocalDate.of(2026, 4, 12);
    public static final int DEFAULT_GUESTS = 2;

    /**
     * 기본 요금 조회 쿼리 (숙소ID=1, 2박, 2명)
     */
    public static FetchRateQuery aFetchRateQuery() {
        return new FetchRateQuery(
                DEFAULT_PROPERTY_ID,
                DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_GUESTS
        );
    }

    /**
     * 지정 숙소 ID의 요금 조회 쿼리
     */
    public static FetchRateQuery aFetchRateQueryForProperty(long propertyId) {
        return new FetchRateQuery(
                PropertyId.of(propertyId),
                DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_GUESTS
        );
    }
}

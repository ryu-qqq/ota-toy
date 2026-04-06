package com.ryuqq.otatoy.api.customer.fixture;

import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyFixture;

import java.util.List;

/**
 * Customer 숙소 검색 API 테스트용 Fixture.
 * 요청 파라미터와 Mock 응답 데이터를 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class CustomerSearchFixture {

    private CustomerSearchFixture() {}

    // === 요청 파라미터 상수 ===

    public static final String CHECK_IN = "2026-05-01";
    public static final String CHECK_OUT = "2026-05-02";
    public static final String GUESTS = "2";
    public static final String SIZE = "10";
    public static final String REGION = "서울";
    public static final String CURSOR = "5";

    // === Mock 응답 데이터 ===

    /**
     * 숙소 검색 결과 (2건, 다음 페이지 있음)
     */
    public static CustomerPropertySliceResult sliceResult() {
        var property1 = PropertyFixture.reconstitutedPropertyWithId(1L);
        var property2 = PropertyFixture.reconstitutedPropertyWithId(2L);

        List<PropertySummary> content = List.of(
            PropertySummary.of(property1, Money.of(80000)),
            PropertySummary.of(property2, Money.of(120000))
        );

        return CustomerPropertySliceResult.of(content, new SliceMeta(true, 2L));
    }

    /**
     * 빈 검색 결과
     */
    public static CustomerPropertySliceResult emptySliceResult() {
        return CustomerPropertySliceResult.empty();
    }
}

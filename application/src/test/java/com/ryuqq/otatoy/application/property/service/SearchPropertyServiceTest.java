package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.query.SearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.query.SearchPropertyQueryFixture;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * SearchPropertyService 단위 테스트.
 * PropertySearchReadManager를 Mock으로 대체하여
 * Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SearchPropertyServiceTest {

    @Mock
    PropertySearchReadManager propertySearchReadManager;

    @InjectMocks
    SearchPropertyService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("검색 쿼리를 Criteria로 변환하여 ReadManager에 전달하고 결과를 반환한다")
        void shouldDelegateToReadManagerAndReturnResult() {
            // given
            SearchPropertyQuery query = SearchPropertyQueryFixture.aSearchPropertyQuery();
            PropertySummary summary = new PropertySummary(
                    PropertyId.of(1L),
                    PropertyName.of("테스트 호텔"),
                    PropertyTypeId.of(1L),
                    Location.of("서울시 강남구", 37.5665, 126.978, "강남", "서울"),
                    Money.of(100_000)
            );
            SliceResult<PropertySummary> expected = SliceResult.of(
                    List.of(summary),
                    new SliceMeta(false, null)
            );
            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(expected);

            // when
            SliceResult<PropertySummary> result = service.execute(query);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Query DTO의 필드가 Criteria에 올바르게 매핑된다")
        void shouldMapQueryFieldsToCriteriaCorrectly() {
            // given
            SearchPropertyQuery query = SearchPropertyQueryFixture.aSearchPropertyQuery();
            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(SliceResult.empty());

            // when
            service.execute(query);

            // then
            ArgumentCaptor<PropertySliceCriteria> captor = ArgumentCaptor.forClass(PropertySliceCriteria.class);
            then(propertySearchReadManager).should().searchByCondition(captor.capture());

            PropertySliceCriteria criteria = captor.getValue();
            assertThat(criteria.region()).isEqualTo("서울");
            assertThat(criteria.checkIn()).isEqualTo(query.checkIn());
            assertThat(criteria.checkOut()).isEqualTo(query.checkOut());
            assertThat(criteria.guests()).isEqualTo(query.guests());
            assertThat(criteria.size()).isEqualTo(query.size());
        }
    }

    @Nested
    @DisplayName("빈 결과 흐름")
    class EmptyResult {

        @Test
        @DisplayName("검색 결과가 없으면 빈 SliceResult를 반환한다")
        void shouldReturnEmptySliceResultWhenNoMatch() {
            // given
            SearchPropertyQuery query = SearchPropertyQueryFixture.aSearchPropertyQuery();
            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(SliceResult.empty());

            // when
            SliceResult<PropertySummary> result = service.execute(query);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("페이지네이션 흐름")
    class Pagination {

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext=true와 nextCursor를 반환한다")
        void shouldReturnHasNextTrueWithCursor() {
            // given
            SearchPropertyQuery query = SearchPropertyQueryFixture.aSearchPropertyQuery();
            SliceResult<PropertySummary> expected = SliceResult.of(
                    List.of(new PropertySummary(
                            PropertyId.of(1L), PropertyName.of("호텔"),
                            PropertyTypeId.of(1L), null, Money.of(80_000)
                    )),
                    new SliceMeta(true, 10L)
            );
            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(expected);

            // when
            SliceResult<PropertySummary> result = service.execute(query);

            // then
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(10L);
        }
    }
}

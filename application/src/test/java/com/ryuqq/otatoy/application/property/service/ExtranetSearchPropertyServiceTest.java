package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.factory.PropertySearchCriteriaFactory;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ExtranetSearchPropertyServiceTest {

    @Spy
    PropertySearchCriteriaFactory criteriaFactory;

    @Mock
    PropertySearchReadManager propertySearchReadManager;

    @Spy
    PropertySearchResultAssembler assembler;

    @InjectMocks
    ExtranetSearchPropertyService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Factory로 Criteria 변환 후 ReadManager에 위임하고 Assembler로 변환한다")
        void shouldDelegateToReadManagerAndAssembleThenReturnResult() {
            // given
            ExtranetSearchPropertyQuery query = new ExtranetSearchPropertyQuery(
                    PartnerId.of(1L), 20, null);

            Property property = createProperty(1L, 1L);
            SliceResult<Property> domainResult = SliceResult.of(
                    List.of(property), new SliceMeta(false, null));

            given(propertySearchReadManager.searchByCriteria(any(ExtranetPropertySliceCriteria.class)))
                    .willReturn(domainResult);

            // when
            ExtranetPropertySliceResult result = service.execute(query);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(result.hasNext()).isFalse();

            then(assembler).should().toExtranetResult(domainResult);
        }

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext가 true이다")
        void shouldReturnHasNextTrueWhenMoreResults() {
            // given
            ExtranetSearchPropertyQuery query = new ExtranetSearchPropertyQuery(
                    PartnerId.of(1L), 1, null);

            Property property = createProperty(1L, 1L);
            SliceResult<Property> domainResult = SliceResult.of(
                    List.of(property), new SliceMeta(true, 1L));

            given(propertySearchReadManager.searchByCriteria(any(ExtranetPropertySliceCriteria.class)))
                    .willReturn(domainResult);

            // when
            ExtranetPropertySliceResult result = service.execute(query);

            // then
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("빈 결과")
    class EmptyResult {

        @Test
        @DisplayName("결과가 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyResultWhenNoMatch() {
            // given
            given(propertySearchReadManager.searchByCriteria(any(ExtranetPropertySliceCriteria.class)))
                    .willReturn(SliceResult.empty());

            // when
            ExtranetPropertySliceResult result = service.execute(
                    new ExtranetSearchPropertyQuery(PartnerId.of(1L), 20, null));

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReadManager 호출 검증")
    class ManagerCall {

        @Test
        @DisplayName("Factory가 Query를 Criteria로 변환하여 ReadManager에 전달한다")
        void shouldPassCriteriaToReadManager() {
            // given
            ExtranetSearchPropertyQuery query = new ExtranetSearchPropertyQuery(
                    PartnerId.of(1L), 20, 100L);

            given(propertySearchReadManager.searchByCriteria(any(ExtranetPropertySliceCriteria.class)))
                    .willReturn(SliceResult.empty());

            // when
            service.execute(query);

            // then
            then(propertySearchReadManager).should()
                    .searchByCriteria(any(ExtranetPropertySliceCriteria.class));
        }
    }

    private Property createProperty(Long id, Long partnerId) {
        return Property.reconstitute(
                PropertyId.of(id),
                PartnerId.of(partnerId),
                BrandId.of(10L),
                PropertyTypeId.of(1L),
                PropertyName.of("테스트 호텔"),
                PropertyDescription.of("테스트 설명"),
                Location.of("서울시 강남구", 37.5665, 126.978, "강남", "서울"),
                null,
                PropertyStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }
}

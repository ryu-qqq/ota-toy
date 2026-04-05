package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.factory.PropertySearchCriteriaFactory;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CustomerSearchPropertyServiceTest {

    @Spy
    PropertySearchCriteriaFactory criteriaFactory;

    @Mock
    PropertySearchReadManager propertySearchReadManager;

    @Spy
    PropertySearchResultAssembler assembler;

    @InjectMocks
    CustomerSearchPropertyService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("검색 쿼리를 Factory로 Criteria 변환 후 ReadManager에 위임하고 Assembler로 변환한다")
        void shouldDelegateToReadManagerAndAssemblerThenReturnResult() {
            // given
            CustomerSearchPropertyQuery query = createQuery();
            Property property = createProperty(1L);
            SliceResult<Property> domainResult = SliceResult.of(
                    List.of(property), new SliceMeta(false, null));

            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(domainResult);

            // when
            CustomerPropertySliceResult result = service.execute(query);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(result.hasNext()).isFalse();

            // Assembler 호출 검증
            then(assembler).should().toCustomerResult(domainResult);
        }

        @Test
        @DisplayName("Factory가 Query를 Criteria로 올바르게 변환한다")
        void shouldMapQueryFieldsToCriteriaViaFactory() {
            // given
            CustomerSearchPropertyQuery query = createQuery();
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
        }
    }

    @Nested
    @DisplayName("빈 결과")
    class EmptyResult {

        @Test
        @DisplayName("검색 결과가 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyResultWhenNoMatch() {
            // given
            given(propertySearchReadManager.searchByCondition(any(PropertySliceCriteria.class)))
                    .willReturn(SliceResult.empty());

            // when
            CustomerPropertySliceResult result = service.execute(createQuery());

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    private CustomerSearchPropertyQuery createQuery() {
        return new CustomerSearchPropertyQuery(
                null, "서울", null,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3),
                2, null, null, List.of(), false, null,
                null, null, 20, null
        );
    }

    private Property createProperty(Long id) {
        return Property.reconstitute(
                PropertyId.of(id),
                PartnerId.of(1L),
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

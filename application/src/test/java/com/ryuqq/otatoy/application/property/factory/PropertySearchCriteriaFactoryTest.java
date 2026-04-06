package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertySearchCriteriaFactory 단위 테스트.
 * Query DTO에서 Domain Criteria로의 변환을 검증한다.
 * 외부 의존성이 없으므로 Mock 없이 순수 단위 테스트로 작성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class PropertySearchCriteriaFactoryTest {

    PropertySearchCriteriaFactory factory = new PropertySearchCriteriaFactory();

    @Nested
    @DisplayName("Extranet 검색 조건 변환")
    class ExtranetCriteria {

        @Test
        @DisplayName("ExtranetSearchPropertyQuery가 ExtranetPropertySliceCriteria로 올바르게 변환된다")
        void shouldConvertExtranetQueryToCriteria() {
            // given
            ExtranetSearchPropertyQuery query = new ExtranetSearchPropertyQuery(
                PartnerId.of(5L), 20, 100L
            );

            // when
            ExtranetPropertySliceCriteria criteria = factory.createForExtranet(query);

            // then
            assertThat(criteria.partnerId()).isEqualTo(PartnerId.of(5L));
            assertThat(criteria.size()).isEqualTo(20);
            assertThat(criteria.cursor()).isEqualTo(100L);
        }

        @Test
        @DisplayName("cursor가 null인 Query도 정상 변환된다")
        void shouldConvertWithNullCursor() {
            // given
            ExtranetSearchPropertyQuery query = new ExtranetSearchPropertyQuery(
                PartnerId.of(1L), 10, null
            );

            // when
            ExtranetPropertySliceCriteria criteria = factory.createForExtranet(query);

            // then
            assertThat(criteria.cursor()).isNull();
        }
    }

    @Nested
    @DisplayName("Customer 검색 조건 변환")
    class CustomerCriteria {

        @Test
        @DisplayName("CustomerSearchPropertyQuery가 PropertySliceCriteria로 올바르게 변환된다")
        void shouldConvertCustomerQueryToCriteria() {
            // given
            CustomerSearchPropertyQuery query = new CustomerSearchPropertyQuery(
                "서울 호텔",
                "강남",
                PropertyTypeId.of(1L),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                2,
                Money.of(50000),
                Money.of(200000),
                List.of(AmenityType.WIFI, AmenityType.PARKING),
                true,
                4,
                PropertySortKey.PRICE_LOW,
                SortDirection.ASC,
                20,
                50L
            );

            // when
            PropertySliceCriteria criteria = factory.create(query);

            // then
            assertThat(criteria.keyword()).isEqualTo("서울 호텔");
            assertThat(criteria.region()).isEqualTo("강남");
            assertThat(criteria.propertyTypeId()).isEqualTo(PropertyTypeId.of(1L));
            assertThat(criteria.checkIn()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(criteria.checkOut()).isEqualTo(LocalDate.of(2026, 5, 3));
            assertThat(criteria.guests()).isEqualTo(2);
            assertThat(criteria.minPrice()).isEqualTo(Money.of(50000));
            assertThat(criteria.maxPrice()).isEqualTo(Money.of(200000));
            assertThat(criteria.amenityTypes()).containsExactly(AmenityType.WIFI, AmenityType.PARKING);
            assertThat(criteria.freeCancellationOnly()).isTrue();
            assertThat(criteria.starRating()).isEqualTo(4);
            assertThat(criteria.sortKey()).isEqualTo(PropertySortKey.PRICE_LOW);
            assertThat(criteria.direction()).isEqualTo(SortDirection.ASC);
            assertThat(criteria.size()).isEqualTo(20);
            assertThat(criteria.cursor()).isEqualTo(50L);
        }

        @Test
        @DisplayName("선택 필드가 null인 Query도 정상 변환된다")
        void shouldConvertWithNullOptionalFields() {
            // given
            CustomerSearchPropertyQuery query = new CustomerSearchPropertyQuery(
                null,
                null,
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                1,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                10,
                null
            );

            // when
            PropertySliceCriteria criteria = factory.create(query);

            // then
            assertThat(criteria.keyword()).isNull();
            assertThat(criteria.region()).isNull();
            assertThat(criteria.propertyTypeId()).isNull();
            assertThat(criteria.minPrice()).isNull();
            assertThat(criteria.maxPrice()).isNull();
            assertThat(criteria.amenityTypes()).isEmpty();
            assertThat(criteria.starRating()).isNull();
            assertThat(criteria.sortKey()).isEqualTo(PropertySortKey.PRICE_LOW);
            assertThat(criteria.direction()).isEqualTo(SortDirection.ASC);
            assertThat(criteria.cursor()).isNull();
        }
    }
}

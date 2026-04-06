package com.ryuqq.otatoy.api.customer.search;

import com.ryuqq.otatoy.api.customer.search.dto.request.SearchPropertyApiRequest;
import com.ryuqq.otatoy.api.customer.search.dto.response.PropertySummaryApiResponse;
import com.ryuqq.otatoy.api.customer.search.mapper.SearchPropertyApiMapper;
import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SearchPropertyApiMapper 단위 테스트.
 * Request -> Domain Query, Result -> Response 변환 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class SearchPropertyApiMapperTest {

    // =========================================================================
    // toQuery: SearchPropertyApiRequest -> CustomerSearchPropertyQuery
    // =========================================================================

    @Nested
    @DisplayName("toQuery - Request -> Query 변환")
    class ToQuery {

        @Nested
        @DisplayName("필수 필드 매핑")
        class RequiredFields {

            @Test
            @DisplayName("체크인/체크아웃/투숙인원이 올바르게 매핑된다")
            void shouldMapRequiredFields() {
                // given
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 3),
                    2,
                    null, null, null, null, null,
                    null, null, 20, null
                );

                // when
                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                // then
                assertThat(query.checkIn()).isEqualTo(LocalDate.of(2026, 5, 1));
                assertThat(query.checkOut()).isEqualTo(LocalDate.of(2026, 5, 3));
                assertThat(query.guests()).isEqualTo(2);
                assertThat(query.size()).isEqualTo(20);
            }
        }

        @Nested
        @DisplayName("선택 필드 매핑")
        class OptionalFields {

            @Test
            @DisplayName("keyword, region이 올바르게 매핑된다")
            void shouldMapKeywordAndRegion() {
                var request = new SearchPropertyApiRequest(
                    "서울 호텔", "서울", null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.keyword()).isEqualTo("서울 호텔");
                assertThat(query.region()).isEqualTo("서울");
            }

            @Test
            @DisplayName("propertyTypeId가 null이면 Query의 propertyTypeId도 null이다")
            void shouldMapNullPropertyTypeId() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.propertyTypeId()).isNull();
            }

            @Test
            @DisplayName("propertyTypeId가 있으면 PropertyTypeId VO로 변환된다")
            void shouldMapPropertyTypeId() {
                var request = new SearchPropertyApiRequest(
                    null, null, 5L,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.propertyTypeId()).isEqualTo(PropertyTypeId.of(5L));
            }

            @Test
            @DisplayName("starRating, freeCancellationOnly, cursor가 올바르게 매핑된다")
            void shouldMapFilterFields() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, true, 4,
                    null, null, 10, 100L
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.freeCancellationOnly()).isTrue();
                assertThat(query.starRating()).isEqualTo(4);
                assertThat(query.cursor()).isEqualTo(100L);
            }

            @Test
            @DisplayName("freeCancellationOnly가 null이면 false로 변환된다")
            void shouldMapNullFreeCancellationOnlyToFalse() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.freeCancellationOnly()).isFalse();
            }
        }

        @Nested
        @DisplayName("Money 변환 (BigDecimal -> Money VO)")
        class MoneyConversion {

            @Test
            @DisplayName("minPrice, maxPrice가 있으면 Money VO로 변환된다")
            void shouldConvertPriceToMoney() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    BigDecimal.valueOf(50000), BigDecimal.valueOf(200000),
                    null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.minPrice()).isEqualTo(Money.of(BigDecimal.valueOf(50000)));
                assertThat(query.maxPrice()).isEqualTo(Money.of(BigDecimal.valueOf(200000)));
            }

            @Test
            @DisplayName("minPrice, maxPrice가 null이면 Query에서도 null이다")
            void shouldMapNullPriceAsNull() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.minPrice()).isNull();
                assertThat(query.maxPrice()).isNull();
            }
        }

        @Nested
        @DisplayName("Enum 변환")
        class EnumConversion {

            @Test
            @DisplayName("amenityTypes 문자열 리스트가 AmenityType Enum 리스트로 변환된다")
            void shouldConvertAmenityTypes() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null,
                    List.of("POOL", "PARKING", "WIFI"),
                    null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.amenityTypes()).containsExactly(
                    AmenityType.POOL, AmenityType.PARKING, AmenityType.WIFI
                );
            }

            @Test
            @DisplayName("amenityTypes가 null이면 빈 리스트로 변환된다")
            void shouldConvertNullAmenityTypesToEmptyList() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.amenityTypes()).isEmpty();
            }

            @Test
            @DisplayName("amenityTypes가 빈 리스트이면 빈 리스트로 변환된다")
            void shouldConvertEmptyAmenityTypesToEmptyList() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, List.of(), null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.amenityTypes()).isEmpty();
            }

            @Test
            @DisplayName("잘못된 amenityType 문자열이면 IllegalArgumentException 발생")
            void shouldThrowOnInvalidAmenityType() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null,
                    List.of("INVALID_TYPE"),
                    null, null,
                    null, null, 10, null
                );

                assertThatThrownBy(() -> SearchPropertyApiMapper.toQuery(request))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("sortKey 문자열이 PropertySortKey Enum으로 변환된다")
            void shouldConvertSortKey() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    "PRICE_LOW", null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.sortKey()).isEqualTo(PropertySortKey.PRICE_LOW);
            }

            @Test
            @DisplayName("sortKey가 null이면 Query의 sortKey도 null이다")
            void shouldMapNullSortKey() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.sortKey()).isNull();
            }

            @Test
            @DisplayName("sortKey가 빈 문자열이면 null로 변환된다")
            void shouldMapBlankSortKeyToNull() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    "  ", null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.sortKey()).isNull();
            }

            @Test
            @DisplayName("direction 문자열이 SortDirection Enum으로 변환된다")
            void shouldConvertDirection() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, "ASC", 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.direction()).isEqualTo(SortDirection.ASC);
            }

            @Test
            @DisplayName("direction이 null이면 Query의 direction도 null이다")
            void shouldMapNullDirection() {
                var request = new SearchPropertyApiRequest(
                    null, null, null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 2),
                    1,
                    null, null, null, null, null,
                    null, null, 10, null
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.direction()).isNull();
            }
        }

        @Nested
        @DisplayName("전체 필드 매핑 통합")
        class FullMapping {

            @Test
            @DisplayName("모든 필드가 설정된 요청이 올바르게 변환된다")
            void shouldMapAllFieldsCorrectly() {
                var request = new SearchPropertyApiRequest(
                    "강남 호텔", "서울", 3L,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 3),
                    2,
                    BigDecimal.valueOf(80000), BigDecimal.valueOf(300000),
                    List.of("POOL", "FITNESS"),
                    true, 5,
                    "PRICE_HIGH", "DESC", 15, 50L
                );

                CustomerSearchPropertyQuery query = SearchPropertyApiMapper.toQuery(request);

                assertThat(query.keyword()).isEqualTo("강남 호텔");
                assertThat(query.region()).isEqualTo("서울");
                assertThat(query.propertyTypeId()).isEqualTo(PropertyTypeId.of(3L));
                assertThat(query.checkIn()).isEqualTo(LocalDate.of(2026, 5, 1));
                assertThat(query.checkOut()).isEqualTo(LocalDate.of(2026, 5, 3));
                assertThat(query.guests()).isEqualTo(2);
                assertThat(query.minPrice()).isEqualTo(Money.of(BigDecimal.valueOf(80000)));
                assertThat(query.maxPrice()).isEqualTo(Money.of(BigDecimal.valueOf(300000)));
                assertThat(query.amenityTypes()).containsExactly(AmenityType.POOL, AmenityType.FITNESS);
                assertThat(query.freeCancellationOnly()).isTrue();
                assertThat(query.starRating()).isEqualTo(5);
                assertThat(query.sortKey()).isEqualTo(PropertySortKey.PRICE_HIGH);
                assertThat(query.direction()).isEqualTo(SortDirection.DESC);
                assertThat(query.size()).isEqualTo(15);
                assertThat(query.cursor()).isEqualTo(50L);
            }
        }
    }

    // =========================================================================
    // toApiResponse: PropertySummary -> PropertySummaryApiResponse
    // =========================================================================

    @Nested
    @DisplayName("toApiResponse - PropertySummary -> PropertySummaryApiResponse 변환")
    class ToApiResponse {

        @Test
        @DisplayName("PropertySummary의 모든 필드가 올바르게 API 응답으로 변환된다")
        void shouldMapAllFieldsToResponse() {
            // given
            var summary = new PropertySummary(
                PropertyId.of(1L),
                PropertyName.of("테스트 호텔"),
                PropertyTypeId.of(2L),
                Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
                Money.of(BigDecimal.valueOf(120000))
            );

            // when
            PropertySummaryApiResponse response = SearchPropertyApiMapper.toApiResponse(summary);

            // then
            assertThat(response.propertyId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("테스트 호텔");
            assertThat(response.propertyTypeId()).isEqualTo(2L);
            assertThat(response.address()).isEqualTo("서울시 강남구");
            assertThat(response.latitude()).isEqualTo(37.5);
            assertThat(response.longitude()).isEqualTo(127.0);
            assertThat(response.region()).isEqualTo("서울");
            assertThat(response.lowestPrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));
        }

        @Test
        @DisplayName("lowestPrice가 null이면 응답의 lowestPrice도 null이다")
        void shouldMapNullLowestPrice() {
            var summary = new PropertySummary(
                PropertyId.of(1L),
                PropertyName.of("테스트 호텔"),
                PropertyTypeId.of(2L),
                Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
                null
            );

            PropertySummaryApiResponse response = SearchPropertyApiMapper.toApiResponse(summary);

            assertThat(response.lowestPrice()).isNull();
        }
    }
}

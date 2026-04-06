package com.ryuqq.otatoy.application.property.assembler;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.PropertyDetailBundle;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertySearchResultAssembler 단위 테스트.
 * 도메인 객체에서 Application DTO로의 변환을 검증한다.
 * 외부 의존성이 없으므로 Mock 없이 순수 단위 테스트로 작성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class PropertySearchResultAssemblerTest {

    PropertySearchResultAssembler assembler = new PropertySearchResultAssembler();

    @Nested
    @DisplayName("고객 검색 결과 변환")
    class CustomerResult {

        @Test
        @DisplayName("Property 목록이 CustomerPropertySliceResult로 올바르게 변환된다")
        void shouldConvertToCustomerResult() {
            // given
            Property property1 = PropertyFixture.reconstitutedPropertyWithId(1L);
            Property property2 = PropertyFixture.reconstitutedPropertyWithId(2L);
            SliceMeta meta = new SliceMeta(true, 2L);
            SliceResult<Property> domainResult = SliceResult.of(List.of(property1, property2), meta);

            // when
            CustomerPropertySliceResult result = assembler.toCustomerResult(domainResult);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Property의 기본 필드가 PropertySummary에 올바르게 매핑된다")
        void shouldMapPropertyFieldsToSummary() {
            // given
            Property property = PropertyFixture.reconstitutedPropertyWithId(1L);
            SliceResult<Property> domainResult = SliceResult.of(
                List.of(property), new SliceMeta(false, null)
            );

            // when
            CustomerPropertySliceResult result = assembler.toCustomerResult(domainResult);

            // then
            PropertySummary summary = result.content().get(0);
            assertThat(summary.propertyId()).isEqualTo(property.id());
            assertThat(summary.name()).isEqualTo(property.name());
            assertThat(summary.propertyTypeId()).isEqualTo(property.propertyTypeId());
            assertThat(summary.location()).isEqualTo(property.location());
            assertThat(summary.lowestPrice()).isEqualTo(Money.of(0));
        }

        @Test
        @DisplayName("빈 Property 목록이면 빈 결과가 반환된다")
        void shouldReturnEmptyResultForEmptyList() {
            // given
            SliceResult<Property> domainResult = SliceResult.empty();

            // when
            CustomerPropertySliceResult result = assembler.toCustomerResult(domainResult);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("Extranet 검색 결과 변환")
    class ExtranetResult {

        @Test
        @DisplayName("Property 목록이 ExtranetPropertySliceResult로 올바르게 변환된다")
        void shouldConvertToExtranetResult() {
            // given
            Property property = PropertyFixture.reconstitutedPropertyWithId(1L);
            SliceMeta meta = new SliceMeta(false, null);
            SliceResult<Property> domainResult = SliceResult.of(List.of(property), meta);

            // when
            ExtranetPropertySliceResult result = assembler.toExtranetResult(domainResult);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 Property 목록이면 빈 결과가 반환된다")
        void shouldReturnEmptyResultForEmptyList() {
            // given
            SliceResult<Property> domainResult = SliceResult.empty();

            // when
            ExtranetPropertySliceResult result = assembler.toExtranetResult(domainResult);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("상세 조회 결과 변환")
    class DetailResult {

        @Test
        @DisplayName("PropertyDetailBundle이 PropertyDetail로 올바르게 변환된다")
        void shouldConvertBundleToDetail() {
            // given
            Property property = PropertyFixture.reconstitutedPropertyWithId(1L);
            PropertyPhotos photos = PropertyPhotos.forNew(List.of());
            PropertyAmenities amenities = PropertyAmenities.forNew(List.of());
            PropertyAttributeValues attributeValues = PropertyAttributeValues.forNew(List.of());
            RoomTypes roomTypes = RoomTypes.from(List.of());

            PropertyDetailBundle bundle = new PropertyDetailBundle(
                property, photos, amenities, attributeValues, roomTypes
            );

            // when
            PropertyDetail result = assembler.toDetail(bundle);

            // then
            assertThat(result.property()).isEqualTo(property);
            assertThat(result.photos()).isEqualTo(photos);
            assertThat(result.amenities()).isEqualTo(amenities);
            assertThat(result.attributeValues()).isEqualTo(attributeValues);
            assertThat(result.roomTypes()).isEqualTo(roomTypes);
        }
    }
}

package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommandFixture;
import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * PropertyAmenityFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PropertyAmenityFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    PropertyAmenityFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T10:00:00Z");

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Command의 편의시설 항목이 PropertyAmenities로 올바르게 변환된다")
        void shouldCreateAmenitiesFromCommand() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAmenitiesCommand command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();

            // when
            PropertyAmenities result = factory.createAmenities(command);

            // then
            assertThat(result.size()).isEqualTo(2);
            List<PropertyAmenity> items = result.items();
            assertThat(items.get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(items.get(0).amenityType()).isEqualTo(AmenityType.WIFI);
            assertThat(items.get(0).name()).isEqualTo(AmenityName.of("와이파이"));
            assertThat(items.get(0).additionalPrice()).isEqualTo(Money.of(0));
            assertThat(items.get(0).sortOrder()).isEqualTo(1);

            assertThat(items.get(1).amenityType()).isEqualTo(AmenityType.PARKING);
            assertThat(items.get(1).name()).isEqualTo(AmenityName.of("주차장"));
            assertThat(items.get(1).additionalPrice()).isEqualTo(Money.of(5000));
            assertThat(items.get(1).sortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 모든 편의시설 항목에 설정된다")
        void shouldUseTimeProviderForTimestamps() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAmenitiesCommand command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();

            // when
            PropertyAmenities result = factory.createAmenities(command);

            // then
            result.items().forEach(item ->
                assertThat(item.createdAt()).isEqualTo(FIXED_NOW)
            );
        }

        @Test
        @DisplayName("빈 편의시설 목록이면 빈 PropertyAmenities가 생성된다")
        void shouldCreateEmptyAmenitiesWhenEmptyList() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAmenitiesCommand command = SetPropertyAmenitiesCommand.of(
                PropertyId.of(1L), List.of()
            );

            // when
            PropertyAmenities result = factory.createAmenities(command);

            // then
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("모든 편의시설 항목에 동일한 propertyId가 설정된다")
        void shouldSetSamePropertyIdForAllItems() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAmenitiesCommand command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();

            // when
            PropertyAmenities result = factory.createAmenities(command);

            // then
            result.items().forEach(item ->
                assertThat(item.propertyId()).isEqualTo(PropertyId.of(1L))
            );
        }
    }
}

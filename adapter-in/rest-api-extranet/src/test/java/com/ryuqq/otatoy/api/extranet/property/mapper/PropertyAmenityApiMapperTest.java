package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyAmenitiesApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyAmenityApiMapper 단위 테스트.
 * 편의시설 설정 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("PropertyAmenityApiMapper")
class PropertyAmenityApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Test
        @DisplayName("propertyId와 편의시설 목록이 정확하게 변환된다")
        void shouldMapPropertyIdAndAmenities() {
            // given
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "PARKING", "주차장", BigDecimal.ZERO, 1
                    ),
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "POOL", "수영장", new BigDecimal("15000"), 2
                    )
                )
            );

            // when
            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(10L, request);

            // then
            assertThat(command.propertyId().value()).isEqualTo(10L);
            assertThat(command.amenities()).hasSize(2);
        }

        @Test
        @DisplayName("AmenityType 문자열이 Enum으로 정확하게 변환된다")
        void shouldConvertAmenityTypeStringToEnum() {
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "POOL", "수영장", BigDecimal.ZERO, 0
                    )
                )
            );

            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(1L, request);

            assertThat(command.amenities().get(0).amenityType()).isEqualTo(AmenityType.POOL);
        }

        @Test
        @DisplayName("편의시설 이름이 AmenityName VO로 변환된다")
        void shouldConvertNameToAmenityNameVo() {
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "PARKING", "무료 주차장", BigDecimal.ZERO, 0
                    )
                )
            );

            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(1L, request);

            assertThat(command.amenities().get(0).name().value()).isEqualTo("무료 주차장");
        }

        @Test
        @DisplayName("추가 요금이 Money VO로 변환된다")
        void shouldConvertAdditionalPriceToMoneyVo() {
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "POOL", "수영장", new BigDecimal("15000"), 1
                    )
                )
            );

            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(1L, request);

            assertThat(command.amenities().get(0).additionalPrice().amount())
                .isEqualByComparingTo(new BigDecimal("15000"));
        }

        @Test
        @DisplayName("추가 요금이 0이면 Money(0)으로 변환된다")
        void shouldConvertZeroPriceToMoneyZero() {
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "PARKING", "주차장", BigDecimal.ZERO, 1
                    )
                )
            );

            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(1L, request);

            assertThat(command.amenities().get(0).additionalPrice().amount())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("sortOrder가 그대로 전달된다")
        void shouldPassSortOrderAsIs() {
            var request = new SetPropertyAmenitiesApiRequest(
                List.of(
                    new SetPropertyAmenitiesApiRequest.AmenityItem(
                        "PARKING", "주차장", BigDecimal.ZERO, 3
                    )
                )
            );

            SetPropertyAmenitiesCommand command = PropertyAmenityApiMapper.toCommand(1L, request);

            assertThat(command.amenities().get(0).sortOrder()).isEqualTo(3);
        }
    }
}

package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyApiMapper 단위 테스트.
 * Request DTO -> Application Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("PropertyApiMapper")
class PropertyApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Nested
        @DisplayName("전체 필드가 포함된 요청")
        class AllFields {

            @Test
            @DisplayName("모든 필드가 정확하게 Domain VO로 변환된다")
            void shouldMapAllFieldsToCommand() {
                // given
                var request = new RegisterPropertyApiRequest(
                    1L, 2L, 3L,
                    "테스트 호텔", "좋은 호텔입니다",
                    "서울시 강남구", 37.5665, 126.978,
                    "강남", "서울", "특가 프로모션"
                );

                // when
                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                // then
                assertThat(command.partnerId().value()).isEqualTo(1L);
                assertThat(command.brandId().value()).isEqualTo(2L);
                assertThat(command.propertyTypeId().value()).isEqualTo(3L);
                assertThat(command.name().value()).isEqualTo("테스트 호텔");
                assertThat(command.description().value()).isEqualTo("좋은 호텔입니다");
                assertThat(command.location().address()).isEqualTo("서울시 강남구");
                assertThat(command.location().latitude()).isEqualTo(37.5665);
                assertThat(command.location().longitude()).isEqualTo(126.978);
                assertThat(command.location().neighborhood()).isEqualTo("강남");
                assertThat(command.location().region()).isEqualTo("서울");
                assertThat(command.promotionText().value()).isEqualTo("특가 프로모션");
            }
        }

        @Nested
        @DisplayName("nullable 필드가 null인 요청")
        class NullableFields {

            @Test
            @DisplayName("brandId가 null이면 Command의 brandId도 null이다")
            void shouldMapNullBrandIdToNull() {
                var request = new RegisterPropertyApiRequest(
                    1L, null, 3L,
                    "최소 호텔", null,
                    "서울시 강남구", 37.5665, 126.978,
                    null, null, null
                );

                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                assertThat(command.brandId()).isNull();
            }

            @Test
            @DisplayName("description이 null이면 Command의 description도 null이다")
            void shouldMapNullDescriptionToNull() {
                var request = new RegisterPropertyApiRequest(
                    1L, null, 3L,
                    "최소 호텔", null,
                    "서울시 강남구", 37.5665, 126.978,
                    null, null, null
                );

                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                assertThat(command.description()).isNull();
            }

            @Test
            @DisplayName("promotionText가 null이면 Command의 promotionText도 null이다")
            void shouldMapNullPromotionTextToNull() {
                var request = new RegisterPropertyApiRequest(
                    1L, null, 3L,
                    "최소 호텔", null,
                    "서울시 강남구", 37.5665, 126.978,
                    null, null, null
                );

                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                assertThat(command.promotionText()).isNull();
            }

            @Test
            @DisplayName("모든 nullable 필드가 null이어도 필수 필드는 정상 매핑된다")
            void shouldMapRequiredFieldsEvenWhenNullablesAreNull() {
                var request = new RegisterPropertyApiRequest(
                    1L, null, 3L,
                    "최소 호텔", null,
                    "서울시 강남구", 37.5665, 126.978,
                    null, null, null
                );

                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                assertThat(command.partnerId().value()).isEqualTo(1L);
                assertThat(command.propertyTypeId().value()).isEqualTo(3L);
                assertThat(command.name().value()).isEqualTo("최소 호텔");
                assertThat(command.location().address()).isEqualTo("서울시 강남구");
            }
        }

        @Nested
        @DisplayName("Location 변환")
        class LocationMapping {

            @Test
            @DisplayName("neighborhood, region이 null이어도 Location이 생성된다")
            void shouldCreateLocationWithNullNeighborhoodAndRegion() {
                var request = new RegisterPropertyApiRequest(
                    1L, null, 3L,
                    "호텔", null,
                    "주소", 0.0, 0.0,
                    null, null, null
                );

                RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

                assertThat(command.location().neighborhood()).isNull();
                assertThat(command.location().region()).isNull();
            }
        }
    }
}

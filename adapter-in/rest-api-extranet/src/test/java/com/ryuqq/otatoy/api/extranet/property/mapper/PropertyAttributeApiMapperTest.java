package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyAttributesApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyAttributeApiMapper 단위 테스트.
 * 속성값 설정 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("PropertyAttributeApiMapper")
class PropertyAttributeApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Test
        @DisplayName("propertyId와 속성값 목록이 정확하게 변환된다")
        void shouldMapPropertyIdAndAttributes() {
            // given
            var request = new SetPropertyAttributesApiRequest(
                List.of(
                    new SetPropertyAttributesApiRequest.AttributeItem(1L, "5성"),
                    new SetPropertyAttributesApiRequest.AttributeItem(2L, "2020")
                )
            );

            // when
            SetPropertyAttributesCommand command = PropertyAttributeApiMapper.toCommand(10L, request);

            // then
            assertThat(command.propertyId().value()).isEqualTo(10L);
            assertThat(command.attributes()).hasSize(2);
        }

        @Test
        @DisplayName("propertyTypeAttributeId가 PropertyTypeAttributeId VO로 변환된다")
        void shouldConvertPropertyTypeAttributeIdToVo() {
            var request = new SetPropertyAttributesApiRequest(
                List.of(
                    new SetPropertyAttributesApiRequest.AttributeItem(42L, "테스트값")
                )
            );

            SetPropertyAttributesCommand command = PropertyAttributeApiMapper.toCommand(1L, request);

            assertThat(command.attributes().get(0).propertyTypeAttributeId().value()).isEqualTo(42L);
        }

        @Test
        @DisplayName("value가 그대로 전달된다")
        void shouldPassValueAsIs() {
            var request = new SetPropertyAttributesApiRequest(
                List.of(
                    new SetPropertyAttributesApiRequest.AttributeItem(1L, "한글 값 테스트")
                )
            );

            SetPropertyAttributesCommand command = PropertyAttributeApiMapper.toCommand(1L, request);

            assertThat(command.attributes().get(0).value()).isEqualTo("한글 값 테스트");
        }

        @Test
        @DisplayName("여러 속성값이 순서대로 변환된다")
        void shouldMapMultipleAttributesInOrder() {
            var request = new SetPropertyAttributesApiRequest(
                List.of(
                    new SetPropertyAttributesApiRequest.AttributeItem(1L, "첫번째"),
                    new SetPropertyAttributesApiRequest.AttributeItem(2L, "두번째"),
                    new SetPropertyAttributesApiRequest.AttributeItem(3L, "세번째")
                )
            );

            SetPropertyAttributesCommand command = PropertyAttributeApiMapper.toCommand(1L, request);

            assertThat(command.attributes()).hasSize(3);
            assertThat(command.attributes().get(0).propertyTypeAttributeId().value()).isEqualTo(1L);
            assertThat(command.attributes().get(1).propertyTypeAttributeId().value()).isEqualTo(2L);
            assertThat(command.attributes().get(2).propertyTypeAttributeId().value()).isEqualTo(3L);
        }
    }
}

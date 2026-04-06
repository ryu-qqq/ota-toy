package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommandFixture;
import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.propertytype.manager.PropertyTypeReadManager;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.RequiredPropertyAttributeMissingException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * PropertyAttributesValidator 단위 테스트.
 * PropertyReadManager와 PropertyTypeReadManager를 Mock으로 대체하여
 * 숙소 존재 확인 + 필수 속성 누락 검증을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PropertyAttributesValidatorTest {

    @Mock
    PropertyReadManager propertyReadManager;

    @Mock
    PropertyTypeReadManager propertyTypeReadManager;

    @InjectMocks
    PropertyAttributesValidator validator;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("모든 필수 속성이 포함되어 있으면 예외 없이 통과한다")
        void shouldPassWhenAllRequiredAttributesProvided() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            var property = PropertyFixture.reconstitutedProperty();
            given(propertyReadManager.getById(command.propertyId())).willReturn(property);
            given(propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId()))
                .willReturn(Set.of(PropertyTypeAttributeId.of(100L), PropertyTypeAttributeId.of(200L)));

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("필수 속성이 없는 숙소 유형이면 예외 없이 통과한다")
        void shouldPassWhenNoRequiredAttributes() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            var property = PropertyFixture.reconstitutedProperty();
            given(propertyReadManager.getById(command.propertyId())).willReturn(property);
            given(propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId()))
                .willReturn(Set.of());

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("필수 속성 외 추가 속성이 포함되어 있어도 예외 없이 통과한다")
        void shouldPassWhenExtraAttributesProvided() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommand.of(
                PropertyId.of(1L),
                List.of(
                    SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(100L), "값1"),
                    SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(200L), "값2"),
                    SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(300L), "추가 값")
                )
            );
            var property = PropertyFixture.reconstitutedProperty();
            given(propertyReadManager.getById(command.propertyId())).willReturn(property);
            given(propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId()))
                .willReturn(Set.of(PropertyTypeAttributeId.of(100L)));

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("숙소가 존재하지 않으면 PropertyNotFoundException을 던진다")
        void shouldThrowWhenPropertyNotFound() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            willThrow(new PropertyNotFoundException())
                .given(propertyReadManager).getById(command.propertyId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("숙소 조회 실패 시 필수 속성 검증은 호출되지 않는다")
        void shouldNotCheckRequiredAttributesWhenPropertyNotFound() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            willThrow(new PropertyNotFoundException())
                .given(propertyReadManager).getById(command.propertyId());

            // when
            try {
                validator.validate(command);
            } catch (PropertyNotFoundException ignored) {
            }

            // then
            then(propertyTypeReadManager).should(never()).getRequiredAttributeIds(
                org.mockito.ArgumentMatchers.any()
            );
        }

        @Test
        @DisplayName("필수 속성이 누락되면 RequiredPropertyAttributeMissingException을 던진다")
        void shouldThrowWhenRequiredAttributesMissing() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommand.of(
                PropertyId.of(1L),
                List.of(
                    SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(100L), "값1")
                )
            );
            var property = PropertyFixture.reconstitutedProperty();
            given(propertyReadManager.getById(command.propertyId())).willReturn(property);
            given(propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId()))
                .willReturn(Set.of(
                    PropertyTypeAttributeId.of(100L),
                    PropertyTypeAttributeId.of(200L),
                    PropertyTypeAttributeId.of(300L)
                ));

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(RequiredPropertyAttributeMissingException.class)
                .satisfies(ex -> {
                    RequiredPropertyAttributeMissingException missing =
                        (RequiredPropertyAttributeMissingException) ex;
                    assertThat(missing.missingAttributeIds())
                        .containsExactlyInAnyOrder(
                            PropertyTypeAttributeId.of(200L),
                            PropertyTypeAttributeId.of(300L)
                        );
                });
        }

        @Test
        @DisplayName("요청에 속성이 하나도 없고 필수 속성이 있으면 모두 누락으로 처리된다")
        void shouldThrowWhenEmptyAttributesAndRequiredExist() {
            // given
            SetPropertyAttributesCommand command = SetPropertyAttributesCommand.of(
                PropertyId.of(1L), List.of()
            );
            var property = PropertyFixture.reconstitutedProperty();
            given(propertyReadManager.getById(command.propertyId())).willReturn(property);
            given(propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId()))
                .willReturn(Set.of(PropertyTypeAttributeId.of(100L)));

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(RequiredPropertyAttributeMissingException.class);
        }
    }
}

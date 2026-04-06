package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommandFixture;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

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
 * PropertyAttributeValueFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PropertyAttributeValueFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    PropertyAttributeValueFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T10:00:00Z");

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Command의 속성값 항목이 PropertyAttributeValues로 올바르게 변환된다")
        void shouldCreateAttributeValuesFromCommand() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();

            // when
            PropertyAttributeValues result = factory.create(command);

            // then
            assertThat(result.size()).isEqualTo(2);
            List<PropertyAttributeValue> items = result.items();
            assertThat(items.get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(items.get(0).propertyTypeAttributeId()).isEqualTo(PropertyTypeAttributeId.of(100L));
            assertThat(items.get(0).value()).isEqualTo("2성급");

            assertThat(items.get(1).propertyTypeAttributeId()).isEqualTo(PropertyTypeAttributeId.of(200L));
            assertThat(items.get(1).value()).isEqualTo("14:00");
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 모든 속성값 항목에 설정된다")
        void shouldUseTimeProviderForTimestamps() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();

            // when
            PropertyAttributeValues result = factory.create(command);

            // then
            result.items().forEach(item ->
                assertThat(item.createdAt()).isEqualTo(FIXED_NOW)
            );
        }

        @Test
        @DisplayName("빈 속성값 목록이면 빈 PropertyAttributeValues가 생성된다")
        void shouldCreateEmptyValuesWhenEmptyList() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAttributesCommand command = SetPropertyAttributesCommand.of(
                PropertyId.of(1L), List.of()
            );

            // when
            PropertyAttributeValues result = factory.create(command);

            // then
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("모든 속성값 항목에 동일한 propertyId가 설정된다")
        void shouldSetSamePropertyIdForAllItems() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyAttributesCommand command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();

            // when
            PropertyAttributeValues result = factory.create(command);

            // then
            result.items().forEach(item ->
                assertThat(item.propertyId()).isEqualTo(PropertyId.of(1L))
            );
        }
    }
}

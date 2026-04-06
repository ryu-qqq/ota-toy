package com.ryuqq.otatoy.application.propertytype.manager;

import com.ryuqq.otatoy.application.propertytype.port.out.PropertyTypeQueryPort;
import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeFixture;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * PropertyTypeReadManager 단위 테스트.
 * PropertyTypeQueryPort를 Mock으로 대체하여 조회/검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PropertyTypeReadManagerTest {

    @Mock
    PropertyTypeQueryPort propertyTypeQueryPort;

    @InjectMocks
    PropertyTypeReadManager manager;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 PropertyType을 반환한다")
        void shouldReturnPropertyTypeWhenFound() {
            // given
            PropertyTypeId id = PropertyTypeId.of(1L);
            PropertyType expected = PropertyTypeFixture.reconstitutedPropertyType();
            given(propertyTypeQueryPort.findById(id)).willReturn(Optional.of(expected));

            // when
            PropertyType result = manager.getById(id);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 PropertyTypeNotFoundException을 던진다")
        void shouldThrowWhenNotFound() {
            // given
            PropertyTypeId id = PropertyTypeId.of(999L);
            given(propertyTypeQueryPort.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> manager.getById(id))
                .isInstanceOf(PropertyTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("verifyExists")
    class VerifyExists {

        @Test
        @DisplayName("존재하는 ID이면 예외 없이 통과한다")
        void shouldPassWhenExists() {
            // given
            PropertyTypeId id = PropertyTypeId.of(1L);
            given(propertyTypeQueryPort.existsById(id)).willReturn(true);

            // when & then
            assertThatCode(() -> manager.verifyExists(id))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("존재하지 않는 ID이면 PropertyTypeNotFoundException을 던진다")
        void shouldThrowWhenNotExists() {
            // given
            PropertyTypeId id = PropertyTypeId.of(999L);
            given(propertyTypeQueryPort.existsById(id)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.verifyExists(id))
                .isInstanceOf(PropertyTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRequiredAttributeIds")
    class GetRequiredAttributeIds {

        @Test
        @DisplayName("필수 속성만 필터링하여 ID Set을 반환한다")
        void shouldReturnOnlyRequiredAttributeIds() {
            // given
            PropertyTypeId propertyTypeId = PropertyTypeId.of(1L);
            PropertyTypeAttribute required = PropertyTypeFixture.requiredAttribute();
            PropertyTypeAttribute optional = PropertyTypeFixture.optionalAttribute();

            given(propertyTypeQueryPort.findAttributesByPropertyTypeId(propertyTypeId))
                .willReturn(List.of(required, optional));

            // when
            Set<PropertyTypeAttributeId> result = manager.getRequiredAttributeIds(propertyTypeId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).contains(required.id());
        }

        @Test
        @DisplayName("필수 속성이 없으면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNoRequiredAttributes() {
            // given
            PropertyTypeId propertyTypeId = PropertyTypeId.of(1L);
            PropertyTypeAttribute optional = PropertyTypeFixture.optionalAttribute();

            given(propertyTypeQueryPort.findAttributesByPropertyTypeId(propertyTypeId))
                .willReturn(List.of(optional));

            // when
            Set<PropertyTypeAttributeId> result = manager.getRequiredAttributeIds(propertyTypeId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("속성이 없으면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNoAttributes() {
            // given
            PropertyTypeId propertyTypeId = PropertyTypeId.of(1L);
            given(propertyTypeQueryPort.findAttributesByPropertyTypeId(propertyTypeId))
                .willReturn(List.of());

            // when
            Set<PropertyTypeAttributeId> result = manager.getRequiredAttributeIds(propertyTypeId);

            // then
            assertThat(result).isEmpty();
        }
    }
}

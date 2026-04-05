package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommandFixture;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PromotionText;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * PropertyFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
class PropertyFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    PropertyFactory propertyFactory;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Command의 필드가 Property에 올바르게 매핑된다")
        void shouldMapCommandFieldsToProperty() {
            // given
            Instant fixedNow = Instant.parse("2026-04-05T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();

            // when
            Property property = propertyFactory.createProperty(command);

            // then
            assertThat(property.partnerId()).isEqualTo(PartnerId.of(1L));
            assertThat(property.brandId()).isEqualTo(BrandId.of(10L));
            assertThat(property.propertyTypeId()).isEqualTo(PropertyTypeId.of(100L));
            assertThat(property.name()).isEqualTo(PropertyName.of("테스트 호텔"));
            assertThat(property.description()).isEqualTo(PropertyDescription.of("테스트 설명"));
            assertThat(property.location()).isEqualTo(
                Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"));
            assertThat(property.promotionText()).isEqualTo(PromotionText.of("특가 이벤트"));
        }

        @Test
        @DisplayName("새로 생성된 Property의 상태는 ACTIVE이다")
        void shouldCreatePropertyWithActiveStatus() {
            // given
            Instant fixedNow = Instant.parse("2026-04-05T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();

            // when
            Property property = propertyFactory.createProperty(command);

            // then
            assertThat(property.status()).isEqualTo(PropertyStatus.ACTIVE);
        }

        @Test
        @DisplayName("새로 생성된 Property의 id는 null이다")
        void shouldCreatePropertyWithNullId() {
            // given
            Instant fixedNow = Instant.parse("2026-04-05T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();

            // when
            Property property = propertyFactory.createProperty(command);

            // then
            assertThat(property.id()).isNull();
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 createdAt, updatedAt에 설정된다")
        void shouldUseTimeProviderForTimestamps() {
            // given
            Instant fixedNow = Instant.parse("2026-04-05T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();

            // when
            Property property = propertyFactory.createProperty(command);

            // then
            assertThat(property.createdAt()).isEqualTo(fixedNow);
            assertThat(property.updatedAt()).isEqualTo(fixedNow);
        }
    }
}

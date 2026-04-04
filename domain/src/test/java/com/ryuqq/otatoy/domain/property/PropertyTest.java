package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    private static final Instant NOW = Instant.now();

    private Property createActiveProperty() {
        return Property.forNew(
                PartnerId.of(1L),
                BrandId.of(1L),
                PropertyTypeId.of(1L),
                PropertyName.of("테스트 숙소"),
                PropertyDescription.of("테스트 설명"),
                Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
                PromotionText.of("프로모션"),
                NOW
        );
    }

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("Property forNew() 정상 생성")
        void shouldCreatePropertySuccessfully() {
            // when
            Property property = createActiveProperty();

            // then
            assertThat(property).isNotNull();
            assertThat(property.id()).isNull();
            assertThat(property.partnerId()).isEqualTo(PartnerId.of(1L));
            assertThat(property.status()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.createdAt()).isEqualTo(NOW);
            assertThat(property.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("partnerId가 null인 Property 생성이 실패해야 한다")
        void shouldFailWhenPartnerIdIsNull() {
            assertThatThrownBy(() -> Property.forNew(
                    null, BrandId.of(1L), PropertyTypeId.of(1L),
                    PropertyName.of("테스트"), PropertyDescription.of("설명"),
                    Location.of("서울시", 37.5, 127.0, "강남", "서울"),
                    PromotionText.of("프로모션"), NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파트너 ID는 필수");
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StateTransition {

        @Test
        @DisplayName("ACTIVE → INACTIVE 비활성화")
        void shouldDeactivateActiveProperty() {
            // given
            Property property = createActiveProperty();
            Instant later = NOW.plusSeconds(60);

            // when
            property.deactivate(later);

            // then
            assertThat(property.status()).isEqualTo(PropertyStatus.INACTIVE);
            assertThat(property.updatedAt()).isEqualTo(later);
        }

        @Test
        @DisplayName("INACTIVE → ACTIVE 활성화")
        void shouldActivateInactiveProperty() {
            // given
            Property property = createActiveProperty();
            Instant deactivateTime = NOW.plusSeconds(60);
            property.deactivate(deactivateTime);

            Instant activateTime = NOW.plusSeconds(120);

            // when
            property.activate(activateTime);

            // then
            assertThat(property.status()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.updatedAt()).isEqualTo(activateTime);
        }

        @Test
        @DisplayName("ACTIVE → INACTIVE → ACTIVE 양방향 전이")
        void shouldTogglePropertyStatus() {
            // given
            Property property = createActiveProperty();
            assertThat(property.isActive()).isTrue();

            // when & then — 비활성화
            Instant t1 = NOW.plusSeconds(60);
            property.deactivate(t1);
            assertThat(property.isActive()).isFalse();

            // when & then — 재활성화
            Instant t2 = NOW.plusSeconds(120);
            property.activate(t2);
            assertThat(property.isActive()).isTrue();
            assertThat(property.updatedAt()).isEqualTo(t2);
        }
    }
}

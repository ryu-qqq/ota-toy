package com.ryuqq.otatoy.domain.partner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerVoTest {

    @Nested
    @DisplayName("T-1: PartnerId")
    class PartnerIdTest {

        @Test
        @DisplayName("of()로 생성할 수 있다")
        void shouldCreateWithOf() {
            PartnerId id = PartnerId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값은 isNew() true를 반환한다")
        void nullValueShouldBeNew() {
            PartnerId id = PartnerId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("값이 있으면 isNew() false를 반환한다")
        void nonNullValueShouldNotBeNew() {
            PartnerId id = PartnerId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("같은 값의 PartnerId는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PartnerId.of(1L)).isEqualTo(PartnerId.of(1L));
        }
    }

    @Nested
    @DisplayName("T-2: PartnerMemberId")
    class PartnerMemberIdTest {

        @Test
        @DisplayName("of()로 생성할 수 있다")
        void shouldCreateWithOf() {
            PartnerMemberId id = PartnerMemberId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값은 isNew() true를 반환한다")
        void nullValueShouldBeNew() {
            assertThat(PartnerMemberId.of(null).isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("T-3: PartnerName")
    class PartnerNameTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            PartnerName name = PartnerName.of("테스트");
            assertThat(name.value()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> PartnerName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파트너명은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> PartnerName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파트너명은 필수");
        }

        @Test
        @DisplayName("같은 값의 PartnerName은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PartnerName.of("테스트")).isEqualTo(PartnerName.of("테스트"));
        }
    }

    @Nested
    @DisplayName("T-4: MemberName")
    class MemberNameTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            MemberName name = MemberName.of("홍길동");
            assertThat(name.value()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> MemberName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("멤버 이름은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> MemberName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("멤버 이름은 필수");
        }
    }
}

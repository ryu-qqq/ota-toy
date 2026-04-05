package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestInfoTest {

    @Nested
    @DisplayName("T-5: VO 생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 모든 필드가 할당된다")
        void shouldCreateSuccessfully() {
            GuestInfo guestInfo = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");

            assertThat(guestInfo.name()).isEqualTo("홍길동");
            assertThat(guestInfo.phone()).isEqualTo(PhoneNumber.of("010-1234-5678"));
            assertThat(guestInfo.email()).isEqualTo(Email.of("hong@test.com"));
        }

        @Test
        @DisplayName("name이 null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> GuestInfo.of(null, "010-1234-5678", "hong@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙객 이름은 필수");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 생성 실패")
        void shouldFailWhenNameIsBlank() {
            assertThatThrownBy(() -> GuestInfo.of("   ", "010-1234-5678", "hong@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙객 이름은 필수");
        }

        @Test
        @DisplayName("phone이 null이어도 생성 성공")
        void shouldAllowNullPhone() {
            GuestInfo guestInfo = GuestInfo.of("홍길동", null, "hong@test.com");

            assertThat(guestInfo.phone()).isNull();
        }

        @Test
        @DisplayName("email이 null이어도 생성 성공")
        void shouldAllowNullEmail() {
            GuestInfo guestInfo = GuestInfo.of("홍길동", "010-1234-5678", null);

            assertThat(guestInfo.email()).isNull();
        }

        @Test
        @DisplayName("phone과 email 모두 null이어도 생성 성공")
        void shouldAllowBothPhoneAndEmailNull() {
            GuestInfo guestInfo = GuestInfo.of("홍길동", null, null);

            assertThat(guestInfo.name()).isEqualTo("홍길동");
            assertThat(guestInfo.phone()).isNull();
            assertThat(guestInfo.email()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 전화번호 포맷이면 생성 실패")
        void shouldFailWhenPhoneFormatIsInvalid() {
            assertThatThrownBy(() -> GuestInfo.of("홍길동", "abc-invalid!", "hong@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숫자와 하이픈만 포함");
        }

        @Test
        @DisplayName("유효하지 않은 이메일 포맷이면 생성 실패")
        void shouldFailWhenEmailFormatIsInvalid() {
            assertThatThrownBy(() -> GuestInfo.of("홍길동", "010-1234-5678", "invalid-email"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 이메일 형식");
        }
    }

    @Nested
    @DisplayName("T-5: VO 동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 GuestInfo는 동등하다 (Record 특성)")
        void shouldBeEqualWithSameValues() {
            GuestInfo g1 = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
            GuestInfo g2 = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");

            assertThat(g1).isEqualTo(g2);
            assertThat(g1.hashCode()).isEqualTo(g2.hashCode());
        }

        @Test
        @DisplayName("다른 이름을 가진 GuestInfo는 동등하지 않다")
        void shouldNotBeEqualWithDifferentName() {
            GuestInfo g1 = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
            GuestInfo g2 = GuestInfo.of("김철수", "010-1234-5678", "hong@test.com");

            assertThat(g1).isNotEqualTo(g2);
        }
    }
}

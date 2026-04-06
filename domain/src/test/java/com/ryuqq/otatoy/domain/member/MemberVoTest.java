package com.ryuqq.otatoy.domain.member;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Member BC의 VO, Enum, ErrorCode, Exception 테스트.
 * MemberTest에서 커버되지 않는 항목을 보강한다.
 */
class MemberVoTest {

    // ========== MemberId ==========

    @Nested
    @DisplayName("MemberId")
    class MemberIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            MemberId id = MemberId.of(42L);
            assertThat(id.value()).isEqualTo(42L);
        }

        @Test
        @DisplayName("null value이면 isNew() true")
        void shouldBeNewWhenNull() {
            MemberId id = MemberId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            MemberId id = MemberId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("forNew()로 생성하면 isNew() true")
        void shouldBeNewViaForNew() {
            MemberId id = MemberId.forNew();
            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("동일 value이면 equals true (Record)")
        void shouldBeEqualWithSameValue() {
            assertThat(MemberId.of(1L)).isEqualTo(MemberId.of(1L));
        }

        @Test
        @DisplayName("다른 value이면 equals false")
        void shouldNotBeEqualWithDifferentValue() {
            assertThat(MemberId.of(1L)).isNotEqualTo(MemberId.of(2L));
        }
    }

    // ========== MemberEmail ==========

    @Nested
    @DisplayName("MemberEmail")
    class MemberEmailTest {

        @Test
        @DisplayName("정상 이메일 생성")
        void shouldCreateSuccessfully() {
            MemberEmail email = MemberEmail.of("user@test.com");
            assertThat(email.value()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("null이면 생성 실패")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> MemberEmail.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 생성 실패")
        void shouldFailWhenEmpty() {
            assertThatThrownBy(() -> MemberEmail.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("공백만 있으면 생성 실패")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> MemberEmail.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("@가 없으면 생성 실패")
        void shouldFailWhenNoAtSign() {
            assertThatThrownBy(() -> MemberEmail.of("usertest.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 이메일 형식");
        }

        @Test
        @DisplayName("200자 이하면 성공")
        void shouldSucceedWhenExactly200Chars() {
            // "a" x 194 + "@b.com" = 200자
            String email = "a".repeat(194) + "@b.com";
            assertThat(email).hasSize(200);
            MemberEmail result = MemberEmail.of(email);
            assertThat(result.value()).hasSize(200);
        }

        @Test
        @DisplayName("201자이면 생성 실패")
        void shouldFailWhenExceeds200Chars() {
            String email = "a".repeat(195) + "@b.com";
            assertThat(email).hasSize(201);
            assertThatThrownBy(() -> MemberEmail.of(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하");
        }

        @Test
        @DisplayName("동일 value이면 equals true (Record)")
        void shouldBeEqualWithSameValue() {
            assertThat(MemberEmail.of("a@b.com")).isEqualTo(MemberEmail.of("a@b.com"));
        }
    }

    // ========== MemberPassword ==========

    @Nested
    @DisplayName("MemberPassword")
    class MemberPasswordTest {

        @Test
        @DisplayName("정상 비밀번호 해시 생성")
        void shouldCreateSuccessfully() {
            MemberPassword pw = MemberPassword.of("$2a$10$hashed");
            assertThat(pw.hashedValue()).isEqualTo("$2a$10$hashed");
        }

        @Test
        @DisplayName("null이면 생성 실패")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> MemberPassword.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호는 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 생성 실패")
        void shouldFailWhenEmpty() {
            assertThatThrownBy(() -> MemberPassword.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호는 필수");
        }

        @Test
        @DisplayName("공백만 있으면 생성 실패")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> MemberPassword.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호는 필수");
        }

        @Test
        @DisplayName("500자 이하면 성공")
        void shouldSucceedWhenExactly500Chars() {
            String pw = "a".repeat(500);
            MemberPassword result = MemberPassword.of(pw);
            assertThat(result.hashedValue()).hasSize(500);
        }

        @Test
        @DisplayName("501자이면 생성 실패")
        void shouldFailWhenExceeds500Chars() {
            String pw = "a".repeat(501);
            assertThatThrownBy(() -> MemberPassword.of(pw))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500자 이하");
        }
    }

    // ========== MemberName ==========

    @Nested
    @DisplayName("MemberName")
    class MemberNameTest {

        @Test
        @DisplayName("정상 이름 생성")
        void shouldCreateSuccessfully() {
            MemberName name = MemberName.of("홍길동");
            assertThat(name.value()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("null이면 생성 실패")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> MemberName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("회원 이름은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 생성 실패")
        void shouldFailWhenEmpty() {
            assertThatThrownBy(() -> MemberName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("회원 이름은 필수");
        }

        @Test
        @DisplayName("공백만 있으면 생성 실패")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> MemberName.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("회원 이름은 필수");
        }

        @Test
        @DisplayName("100자 이하면 성공")
        void shouldSucceedWhenExactly100Chars() {
            String name = "가".repeat(100);
            MemberName result = MemberName.of(name);
            assertThat(result.value()).hasSize(100);
        }

        @Test
        @DisplayName("101자이면 생성 실패")
        void shouldFailWhenExceeds100Chars() {
            String name = "가".repeat(101);
            assertThatThrownBy(() -> MemberName.of(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100자 이하");
        }
    }

    // ========== MemberStatus ==========

    @Nested
    @DisplayName("MemberStatus")
    class MemberStatusTest {

        @Test
        @DisplayName("ACTIVE에서 SUSPENDED로 전이 가능")
        void shouldTransitActiveToSuspended() {
            assertThat(MemberStatus.ACTIVE.canTransitTo(MemberStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED에서 ACTIVE로 전이 가능")
        void shouldTransitSuspendedToActive() {
            assertThat(MemberStatus.SUSPENDED.canTransitTo(MemberStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE에서 ACTIVE로 전이 불가")
        void shouldNotTransitActiveToActive() {
            assertThat(MemberStatus.ACTIVE.canTransitTo(MemberStatus.ACTIVE)).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED에서 SUSPENDED로 전이 불가")
        void shouldNotTransitSuspendedToSuspended() {
            assertThat(MemberStatus.SUSPENDED.canTransitTo(MemberStatus.SUSPENDED)).isFalse();
        }

        @Test
        @DisplayName("transitTo() 성공 시 대상 상태 반환")
        void shouldReturnTargetOnSuccessfulTransit() {
            MemberStatus result = MemberStatus.ACTIVE.transitTo(MemberStatus.SUSPENDED);
            assertThat(result).isEqualTo(MemberStatus.SUSPENDED);
        }

        @Test
        @DisplayName("transitTo() 실패 시 IllegalStateException")
        void shouldThrowOnInvalidTransit() {
            assertThatThrownBy(() -> MemberStatus.ACTIVE.transitTo(MemberStatus.ACTIVE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("전이할 수 없습니다");
        }

        @Test
        @DisplayName("displayName()이 올바르다")
        void shouldHaveCorrectDisplayName() {
            assertThat(MemberStatus.ACTIVE.displayName()).isEqualTo("활성");
            assertThat(MemberStatus.SUSPENDED.displayName()).isEqualTo("정지");
        }
    }

    // ========== MemberRole ==========

    @Nested
    @DisplayName("MemberRole")
    class MemberRoleTest {

        @Test
        @DisplayName("모든 역할이 displayName()을 반환한다")
        void shouldHaveDisplayName() {
            for (MemberRole role : MemberRole.values()) {
                assertThat(role.displayName()).isNotNull();
                assertThat(role.displayName()).isNotBlank();
            }
        }

        @Test
        @DisplayName("CUSTOMER의 displayName은 '고객'이다")
        void customerDisplayName() {
            assertThat(MemberRole.CUSTOMER.displayName()).isEqualTo("고객");
        }

        @Test
        @DisplayName("ADMIN의 displayName은 '관리자'이다")
        void adminDisplayName() {
            assertThat(MemberRole.ADMIN.displayName()).isEqualTo("관리자");
        }
    }

    // ========== MemberErrorCode ==========

    @Nested
    @DisplayName("MemberErrorCode")
    class MemberErrorCodeTest {

        @Test
        @DisplayName("모든 에러 코드가 code, message, category를 반환한다")
        void shouldHaveAllFields() {
            for (MemberErrorCode errorCode : MemberErrorCode.values()) {
                assertThat(errorCode.getCode()).isNotNull().isNotBlank();
                assertThat(errorCode.getMessage()).isNotNull().isNotBlank();
                assertThat(errorCode.getCategory()).isNotNull();
            }
        }

        @Test
        @DisplayName("MEMBER_NOT_FOUND의 코드는 MBR-001이다")
        void memberNotFoundCode() {
            assertThat(MemberErrorCode.MEMBER_NOT_FOUND.getCode()).isEqualTo("MBR-001");
            assertThat(MemberErrorCode.MEMBER_NOT_FOUND.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
        }

        @Test
        @DisplayName("DUPLICATE_EMAIL의 카테고리는 CONFLICT이다")
        void duplicateEmailCategory() {
            assertThat(MemberErrorCode.DUPLICATE_EMAIL.getCategory()).isEqualTo(ErrorCategory.CONFLICT);
        }
    }

    // ========== MemberException / MemberNotFoundException ==========

    @Nested
    @DisplayName("MemberException / MemberNotFoundException")
    class ExceptionTest {

        @Test
        @DisplayName("MemberNotFoundException의 에러 코드는 MEMBER_NOT_FOUND이다")
        void shouldHaveCorrectErrorCode() {
            MemberNotFoundException ex = new MemberNotFoundException();
            assertThat(ex.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("회원을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("MemberNotFoundException은 MemberException의 하위 클래스이다")
        void shouldBeMemberException() {
            MemberNotFoundException ex = new MemberNotFoundException();
            assertThat(ex).isInstanceOf(MemberException.class);
        }

        @Test
        @DisplayName("MemberException은 RuntimeException이다")
        void shouldBeRuntimeException() {
            MemberNotFoundException ex = new MemberNotFoundException();
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}

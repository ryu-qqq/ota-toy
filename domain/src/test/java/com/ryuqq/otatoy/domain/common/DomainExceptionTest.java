package com.ryuqq.otatoy.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    // 테스트용 ErrorCode 구현
    enum TestErrorCode implements ErrorCode {
        TEST_ERROR("TEST_001", "테스트 에러 메시지", ErrorCategory.VALIDATION),
        NOT_FOUND_ERROR("TEST_002", "리소스를 찾을 수 없음", ErrorCategory.NOT_FOUND);

        private final String code;
        private final String message;
        private final ErrorCategory category;

        TestErrorCode(String code, String message, ErrorCategory category) {
            this.code = code;
            this.message = message;
            this.category = category;
        }

        @Override
        public String getCode() { return code; }

        @Override
        public String getMessage() { return message; }

        @Override
        public ErrorCategory getCategory() { return category; }
    }

    // 테스트용 DomainException 구현
    static class TestDomainException extends DomainException {
        TestDomainException(ErrorCode errorCode) {
            super(errorCode);
        }

        TestDomainException(ErrorCode errorCode, Map<String, Object> args) {
            super(errorCode, args);
        }
    }

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("ErrorCode만으로 생성할 수 있다")
        void shouldCreateWithErrorCodeOnly() {
            TestDomainException ex = new TestDomainException(TestErrorCode.TEST_ERROR);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.TEST_ERROR);
            assertThat(ex.getMessage()).isEqualTo("테스트 에러 메시지");
            assertThat(ex.getArgs()).isEmpty();
        }

        @Test
        @DisplayName("ErrorCode와 인자로 생성할 수 있다")
        void shouldCreateWithErrorCodeAndArgs() {
            Map<String, Object> args = Map.of("id", 1L);
            TestDomainException ex = new TestDomainException(TestErrorCode.NOT_FOUND_ERROR, args);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.NOT_FOUND_ERROR);
            assertThat(ex.getArgs()).containsEntry("id", 1L);
        }

        @Test
        @DisplayName("args가 null이면 빈 Map으로 처리된다")
        void shouldHandleNullArgs() {
            TestDomainException ex = new TestDomainException(TestErrorCode.TEST_ERROR, null);

            assertThat(ex.getArgs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("불변성 검증")
    class Immutability {

        @Test
        @DisplayName("args는 불변 Map이다")
        void argsShouldBeUnmodifiable() {
            Map<String, Object> args = Map.of("key", "value");
            TestDomainException ex = new TestDomainException(TestErrorCode.TEST_ERROR, args);

            assertThat(ex.getArgs()).isUnmodifiable();
        }
    }

    @Nested
    @DisplayName("ErrorCode 검증")
    class ErrorCodeTest {

        @Test
        @DisplayName("ErrorCode는 code, message, category를 반환한다")
        void shouldReturnCodeMessageCategory() {
            ErrorCode code = TestErrorCode.TEST_ERROR;

            assertThat(code.getCode()).isEqualTo("TEST_001");
            assertThat(code.getMessage()).isEqualTo("테스트 에러 메시지");
            assertThat(code.getCategory()).isEqualTo(ErrorCategory.VALIDATION);
        }
    }

    @Nested
    @DisplayName("ErrorCategory 검증")
    class ErrorCategoryTest {

        @Test
        @DisplayName("각 카테고리는 displayName을 반환한다")
        void shouldReturnDisplayName() {
            assertThat(ErrorCategory.NOT_FOUND.displayName()).isEqualTo("리소스 없음");
            assertThat(ErrorCategory.VALIDATION.displayName()).isEqualTo("검증 실패");
            assertThat(ErrorCategory.CONFLICT.displayName()).isEqualTo("상태 충돌");
            assertThat(ErrorCategory.FORBIDDEN.displayName()).isEqualTo("금지된 행위");
        }

        @Test
        @DisplayName("모든 카테고리가 정의되어 있다")
        void shouldHaveAllCategories() {
            assertThat(ErrorCategory.values()).hasSize(4);
        }
    }
}

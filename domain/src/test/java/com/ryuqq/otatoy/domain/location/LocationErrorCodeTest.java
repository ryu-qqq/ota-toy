package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LocationErrorCode, LocationException 테스트.
 */
class LocationErrorCodeTest {

    @Nested
    @DisplayName("LocationErrorCode")
    class ErrorCodeTest {

        @Test
        @DisplayName("모든 에러 코드가 code, message, category를 반환한다")
        void shouldHaveAllFields() {
            for (LocationErrorCode errorCode : LocationErrorCode.values()) {
                assertThat(errorCode.getCode()).isNotNull().isNotBlank();
                assertThat(errorCode.getMessage()).isNotNull().isNotBlank();
                assertThat(errorCode.getCategory()).isNotNull();
            }
        }

        @Test
        @DisplayName("LANDMARK_NOT_FOUND의 코드는 LOC-001이다")
        void landmarkNotFoundCode() {
            assertThat(LocationErrorCode.LANDMARK_NOT_FOUND.getCode()).isEqualTo("LOC-001");
            assertThat(LocationErrorCode.LANDMARK_NOT_FOUND.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
        }

        @Test
        @DisplayName("INVALID_LANDMARK_NAME의 카테고리는 VALIDATION이다")
        void invalidLandmarkNameCategory() {
            assertThat(LocationErrorCode.INVALID_LANDMARK_NAME.getCategory()).isEqualTo(ErrorCategory.VALIDATION);
        }

        @Test
        @DisplayName("에러 코드가 LOC- 접두사를 사용한다")
        void shouldUseLOCPrefix() {
            for (LocationErrorCode errorCode : LocationErrorCode.values()) {
                assertThat(errorCode.getCode()).startsWith("LOC-");
            }
        }

        @Test
        @DisplayName("에러 코드 번호가 중복되지 않는다")
        void shouldHaveUniqueCodeNumbers() {
            long distinctCount = java.util.Arrays.stream(LocationErrorCode.values())
                    .map(LocationErrorCode::getCode)
                    .distinct()
                    .count();
            assertThat(distinctCount).isEqualTo(LocationErrorCode.values().length);
        }
    }

    @Nested
    @DisplayName("LocationException")
    class ExceptionTest {

        @Test
        @DisplayName("LocationException은 RuntimeException이다")
        void shouldBeRuntimeException() {
            LocationException ex = new LocationException(LocationErrorCode.LANDMARK_NOT_FOUND);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("getMessage()가 에러 코드의 메시지와 동일하다")
        void shouldHaveCorrectMessage() {
            LocationException ex = new LocationException(LocationErrorCode.LANDMARK_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("랜드마크를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("getErrorCode()가 올바른 에러 코드를 반환한다")
        void shouldHaveCorrectErrorCode() {
            LocationException ex = new LocationException(LocationErrorCode.INVALID_DISTANCE);
            assertThat(ex.getErrorCode()).isEqualTo(LocationErrorCode.INVALID_DISTANCE);
        }
    }
}

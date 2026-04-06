package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccommodationErrorCode 검증")
class AccommodationErrorCodeTest {

    @ParameterizedTest(name = "{0}: 코드={1}, 카테고리={3}")
    @DisplayName("모든 에러코드의 코드, 메시지, 카테고리가 올바르다")
    @MethodSource("provideErrorCodes")
    void shouldHaveCorrectAttributes(AccommodationErrorCode errorCode, String expectedCode,
                                      String expectedMessage, ErrorCategory expectedCategory) {
        assertThat(errorCode.getCode()).isEqualTo(expectedCode);
        assertThat(errorCode.getMessage()).isEqualTo(expectedMessage);
        assertThat(errorCode.getCategory()).isEqualTo(expectedCategory);
    }

    static Stream<Arguments> provideErrorCodes() {
        return Stream.of(
                Arguments.of(AccommodationErrorCode.PROPERTY_NOT_FOUND, "ACC-001", "숙소를 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
                Arguments.of(AccommodationErrorCode.ROOM_TYPE_NOT_FOUND, "ACC-002", "객실 유형을 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
                Arguments.of(AccommodationErrorCode.INVALID_PROPERTY_STATUS, "ACC-004", "유효하지 않은 숙소 상태입니다", ErrorCategory.VALIDATION),
                Arguments.of(AccommodationErrorCode.INVALID_ROOM_TYPE, "ACC-005", "유효하지 않은 객실 정보입니다", ErrorCategory.VALIDATION),
                Arguments.of(AccommodationErrorCode.REQUIRED_ATTRIBUTE_MISSING, "ACC-006", "필수 속성이 누락되었습니다", ErrorCategory.VALIDATION)
        );
    }

    @Test
    @DisplayName("에러코드 개수가 5개이다")
    void shouldHaveFiveErrorCodes() {
        assertThat(AccommodationErrorCode.values()).hasSize(5);
    }
}

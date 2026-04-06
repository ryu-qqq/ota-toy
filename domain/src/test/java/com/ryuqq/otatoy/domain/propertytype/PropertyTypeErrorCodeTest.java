package com.ryuqq.otatoy.domain.propertytype;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PropertyTypeErrorCode 검증")
class PropertyTypeErrorCodeTest {

    @Test
    @DisplayName("PROPERTY_TYPE_NOT_FOUND 에러코드의 코드, 메시지, 카테고리가 올바르다")
    void shouldHaveCorrectPropertyTypeNotFound() {
        PropertyTypeErrorCode code = PropertyTypeErrorCode.PROPERTY_TYPE_NOT_FOUND;

        assertThat(code.getCode()).isEqualTo("PT-001");
        assertThat(code.getMessage()).isEqualTo("숙소 유형을 찾을 수 없습니다");
        assertThat(code.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
    }
}

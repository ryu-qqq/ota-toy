package com.ryuqq.otatoy.domain.propertytype;

import com.ryuqq.otatoy.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PropertyType 예외 검증")
class PropertyTypeExceptionTest {

    @Test
    @DisplayName("PropertyTypeNotFoundException은 DomainException을 상속하며 올바른 에러코드를 포함한다")
    void shouldHaveCorrectErrorCode() {
        PropertyTypeNotFoundException ex = new PropertyTypeNotFoundException();

        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getErrorCode()).isEqualTo(PropertyTypeErrorCode.PROPERTY_TYPE_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("숙소 유형을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("PropertyTypeNotFoundException을 throw하면 DomainException으로 catch할 수 있다")
    void shouldBeCatchableAsDomainException() {
        assertThatThrownBy(() -> { throw new PropertyTypeNotFoundException(); })
                .isInstanceOf(DomainException.class);
    }
}

package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierErrorCodeTest {

    @Test
    @DisplayName("모든 ErrorCode는 SUP- 접두사를 갖는다")
    void allCodesShouldHaveSupPrefix() {
        for (SupplierErrorCode code : SupplierErrorCode.values()) {
            assertThat(code.getCode()).startsWith("SUP-");
        }
    }

    @Test
    @DisplayName("ErrorCode 인터페이스를 구현한다")
    void shouldImplementErrorCode() {
        ErrorCode code = SupplierErrorCode.SUPPLIER_NOT_FOUND;

        assertThat(code.getCode()).isEqualTo("SUP-001");
        assertThat(code.getHttpStatus()).isEqualTo(404);
        assertThat(code.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("코드 중복이 없다")
    void shouldHaveUniqueCode() {
        SupplierErrorCode[] values = SupplierErrorCode.values();
        long uniqueCount = java.util.Arrays.stream(values)
                .map(SupplierErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(values.length);
    }
}

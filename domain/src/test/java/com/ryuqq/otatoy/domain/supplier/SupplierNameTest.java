package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierNameTest {

    @Test
    @DisplayName("정상 생성")
    void shouldCreate() {
        SupplierName name = SupplierName.of("TestSupplier");
        assertThat(name.value()).isEqualTo("TestSupplier");
    }

    @Test
    @DisplayName("null이면 생성 실패")
    void shouldFailWhenNull() {
        assertThatThrownBy(() -> SupplierName.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공급자명");
    }

    @Test
    @DisplayName("blank이면 생성 실패")
    void shouldFailWhenBlank() {
        assertThatThrownBy(() -> SupplierName.of("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공급자명");
    }

    @Test
    @DisplayName("같은 값의 SupplierName은 동등하다")
    void shouldBeEqualWithSameValue() {
        SupplierName name1 = SupplierName.of("ABC");
        SupplierName name2 = SupplierName.of("ABC");

        assertThat(name1).isEqualTo(name2);
    }
}

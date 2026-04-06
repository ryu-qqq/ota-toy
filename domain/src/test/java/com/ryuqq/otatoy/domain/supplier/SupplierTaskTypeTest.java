package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierTaskTypeTest {

    @ParameterizedTest
    @EnumSource(SupplierTaskType.class)
    @DisplayName("모든 작업 유형에 displayName이 존재한다")
    void allTypesHaveDisplayName(SupplierTaskType type) {
        assertThat(type.displayName()).isNotBlank();
    }
}

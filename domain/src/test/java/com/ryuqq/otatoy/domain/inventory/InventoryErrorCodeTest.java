package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryErrorCodeTest {

    @ParameterizedTest
    @EnumSource(InventoryErrorCode.class)
    @DisplayName("모든 InventoryErrorCode는 ErrorCode 인터페이스를 구현한다")
    void shouldImplementErrorCode(InventoryErrorCode errorCode) {
        assertThat(errorCode).isInstanceOf(ErrorCode.class);
        assertThat(errorCode.getCode()).isNotBlank();
        assertThat(errorCode.getMessage()).isNotBlank();
        assertThat(errorCode.getCategory()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(InventoryErrorCode.class)
    @DisplayName("모든 에러 코드는 INV- 접두사를 가진다")
    void shouldHaveInventoryPrefix(InventoryErrorCode errorCode) {
        assertThat(errorCode.getCode()).startsWith("INV-");
    }

    @Test
    @DisplayName("INVENTORY_NOT_FOUND는 NOT_FOUND 카테고리이다")
    void notFoundShouldHaveNotFoundCategory() {
        assertThat(InventoryErrorCode.INVENTORY_NOT_FOUND.getCode()).isEqualTo("INV-001");
        assertThat(InventoryErrorCode.INVENTORY_NOT_FOUND.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
    }

    @Test
    @DisplayName("INVENTORY_EXHAUSTED는 CONFLICT 카테고리이다")
    void exhaustedShouldHaveConflictCategory() {
        assertThat(InventoryErrorCode.INVENTORY_EXHAUSTED.getCode()).isEqualTo("INV-002");
        assertThat(InventoryErrorCode.INVENTORY_EXHAUSTED.getCategory()).isEqualTo(ErrorCategory.CONFLICT);
    }

    @Test
    @DisplayName("INVENTORY_STOP_SELL은 FORBIDDEN 카테고리이다")
    void stopSellShouldHaveForbiddenCategory() {
        assertThat(InventoryErrorCode.INVENTORY_STOP_SELL.getCode()).isEqualTo("INV-003");
        assertThat(InventoryErrorCode.INVENTORY_STOP_SELL.getCategory()).isEqualTo(ErrorCategory.FORBIDDEN);
    }

    @Test
    @DisplayName("INVENTORY_OVERFLOW는 VALIDATION 카테고리이다")
    void overflowShouldHaveValidationCategory() {
        assertThat(InventoryErrorCode.INVENTORY_OVERFLOW.getCode()).isEqualTo("INV-004");
        assertThat(InventoryErrorCode.INVENTORY_OVERFLOW.getCategory()).isEqualTo(ErrorCategory.VALIDATION);
    }

    @Test
    @DisplayName("에러 코드가 4개 존재한다")
    void shouldHaveFourErrorCodes() {
        assertThat(InventoryErrorCode.values()).hasSize(4);
    }
}

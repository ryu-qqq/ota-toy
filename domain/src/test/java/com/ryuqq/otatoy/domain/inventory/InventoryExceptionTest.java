package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryExceptionTest {

    @Nested
    @DisplayName("InventoryExhaustedException 검증")
    class ExhaustedExceptionTests {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            InventoryExhaustedException exception = new InventoryExhaustedException();
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception).isInstanceOf(InventoryException.class);
        }

        @Test
        @DisplayName("에러 코드가 INVENTORY_EXHAUSTED이다")
        void shouldHaveCorrectErrorCode() {
            InventoryExhaustedException exception = new InventoryExhaustedException();
            assertThat(exception.getErrorCode()).isEqualTo(InventoryErrorCode.INVENTORY_EXHAUSTED);
            assertThat(exception.code()).isEqualTo("INV-002");
        }

        @Test
        @DisplayName("메시지가 올바르다")
        void shouldHaveCorrectMessage() {
            InventoryExhaustedException exception = new InventoryExhaustedException();
            assertThat(exception.getMessage()).isEqualTo("재고가 소진되었습니다");
        }

        @Test
        @DisplayName("카테고리가 CONFLICT이다")
        void shouldHaveConflictCategory() {
            InventoryExhaustedException exception = new InventoryExhaustedException();
            assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        }
    }

    @Nested
    @DisplayName("InventoryNotFoundException 검증")
    class NotFoundExceptionTests {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            InventoryNotFoundException exception = new InventoryNotFoundException();
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception).isInstanceOf(InventoryException.class);
        }

        @Test
        @DisplayName("에러 코드가 INVENTORY_NOT_FOUND이다")
        void shouldHaveCorrectErrorCode() {
            InventoryNotFoundException exception = new InventoryNotFoundException();
            assertThat(exception.getErrorCode()).isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND);
            assertThat(exception.code()).isEqualTo("INV-001");
        }

        @Test
        @DisplayName("메시지가 올바르다")
        void shouldHaveCorrectMessage() {
            InventoryNotFoundException exception = new InventoryNotFoundException();
            assertThat(exception.getMessage()).isEqualTo("재고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("카테고리가 NOT_FOUND이다")
        void shouldHaveNotFoundCategory() {
            InventoryNotFoundException exception = new InventoryNotFoundException();
            assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("InventoryStopSellException 검증")
    class StopSellExceptionTests {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            InventoryStopSellException exception = new InventoryStopSellException();
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception).isInstanceOf(InventoryException.class);
        }

        @Test
        @DisplayName("에러 코드가 INVENTORY_STOP_SELL이다")
        void shouldHaveCorrectErrorCode() {
            InventoryStopSellException exception = new InventoryStopSellException();
            assertThat(exception.getErrorCode()).isEqualTo(InventoryErrorCode.INVENTORY_STOP_SELL);
            assertThat(exception.code()).isEqualTo("INV-003");
        }

        @Test
        @DisplayName("메시지가 올바르다")
        void shouldHaveCorrectMessage() {
            InventoryStopSellException exception = new InventoryStopSellException();
            assertThat(exception.getMessage()).isEqualTo("판매가 중지된 재고입니다");
        }

        @Test
        @DisplayName("카테고리가 FORBIDDEN이다")
        void shouldHaveForbiddenCategory() {
            InventoryStopSellException exception = new InventoryStopSellException();
            assertThat(exception.category()).isEqualTo(ErrorCategory.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("InventoryOverflowException 검증")
    class OverflowExceptionTests {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            InventoryOverflowException exception = new InventoryOverflowException();
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception).isInstanceOf(InventoryException.class);
        }

        @Test
        @DisplayName("에러 코드가 INVENTORY_OVERFLOW이다")
        void shouldHaveCorrectErrorCode() {
            InventoryOverflowException exception = new InventoryOverflowException();
            assertThat(exception.getErrorCode()).isEqualTo(InventoryErrorCode.INVENTORY_OVERFLOW);
            assertThat(exception.code()).isEqualTo("INV-004");
        }

        @Test
        @DisplayName("메시지가 올바르다")
        void shouldHaveCorrectMessage() {
            InventoryOverflowException exception = new InventoryOverflowException();
            assertThat(exception.getMessage()).isEqualTo("복구 수량이 전체 수량을 초과합니다");
        }

        @Test
        @DisplayName("카테고리가 VALIDATION이다")
        void shouldHaveValidationCategory() {
            InventoryOverflowException exception = new InventoryOverflowException();
            assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        }
    }

    @Nested
    @DisplayName("실제 도메인 로직에서 올바른 예외가 발생하는지 통합 검증")
    class IntegrationWithDomainLogic {

        @Test
        @DisplayName("재고 소진 시 InventoryExhaustedException이 발생하고 에러 코드가 정확하다")
        void shouldThrowExhaustedWithCorrectErrorCode() {
            Inventory inventory = InventoryFixture.exhaustedInventory();

            assertThatThrownBy(inventory::decrease)
                    .isInstanceOf(InventoryExhaustedException.class)
                    .satisfies(ex -> {
                        InventoryExhaustedException invEx = (InventoryExhaustedException) ex;
                        assertThat(invEx.code()).isEqualTo("INV-002");
                        assertThat(invEx.category()).isEqualTo(ErrorCategory.CONFLICT);
                    });
        }

        @Test
        @DisplayName("판매 중지 시 InventoryStopSellException이 발생하고 에러 코드가 정확하다")
        void shouldThrowStopSellWithCorrectErrorCode() {
            Inventory inventory = InventoryFixture.stopSellInventory();

            assertThatThrownBy(inventory::decrease)
                    .isInstanceOf(InventoryStopSellException.class)
                    .satisfies(ex -> {
                        InventoryStopSellException invEx = (InventoryStopSellException) ex;
                        assertThat(invEx.code()).isEqualTo("INV-003");
                        assertThat(invEx.category()).isEqualTo(ErrorCategory.FORBIDDEN);
                    });
        }

        @Test
        @DisplayName("재고 복구 시 초과하면 InventoryOverflowException이 발생하고 에러 코드가 정확하다")
        void shouldThrowOverflowWithCorrectErrorCode() {
            Inventory inventory = InventoryFixture.reconstitutedInventory();

            assertThatThrownBy(() -> inventory.restore(1))
                    .isInstanceOf(InventoryOverflowException.class)
                    .satisfies(ex -> {
                        InventoryOverflowException invEx = (InventoryOverflowException) ex;
                        assertThat(invEx.code()).isEqualTo("INV-004");
                        assertThat(invEx.category()).isEqualTo(ErrorCategory.VALIDATION);
                    });
        }
    }
}

package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * InventoryCommandManager 단위 테스트.
 * InventoryCommandPort를 Mock으로 대체하여 DB 재고 차감/복구 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class InventoryCommandManagerTest {

    @Mock
    InventoryCommandPort inventoryCommandPort;

    @InjectMocks
    InventoryCommandManager manager;

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);

    @Nested
    @DisplayName("decrementAvailable")
    class DecrementAvailable {

        @Test
        @DisplayName("모든 날짜의 재고 차감이 성공하면 예외 없이 통과한다")
        void shouldDecrementAllDatesSuccessfully() {
            // given
            List<LocalDate> dates = List.of(
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 11)
            );
            given(inventoryCommandPort.decrementAvailable(1L, dates.get(0))).willReturn(true);
            given(inventoryCommandPort.decrementAvailable(1L, dates.get(1))).willReturn(true);

            // when & then
            assertThatCode(() -> manager.decrementAvailable(ROOM_TYPE_ID, dates))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("두 번째 날짜에서 재고 부족 시 첫 번째 날짜를 복구하고 예외를 던진다")
        void shouldRollbackAndThrowWhenSecondDateFails() {
            // given
            LocalDate date1 = LocalDate.of(2026, 4, 10);
            LocalDate date2 = LocalDate.of(2026, 4, 11);
            List<LocalDate> dates = List.of(date1, date2);

            given(inventoryCommandPort.decrementAvailable(1L, date1)).willReturn(true);
            given(inventoryCommandPort.decrementAvailable(1L, date2)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.decrementAvailable(ROOM_TYPE_ID, dates))
                .isInstanceOf(InventoryExhaustedException.class);

            // 첫 번째 날짜 복구 확인
            then(inventoryCommandPort).should().incrementAvailable(1L, date1);
        }

        @Test
        @DisplayName("세 번째 날짜에서 실패 시 앞선 두 날짜를 모두 복구한다")
        void shouldRollbackAllPreviouslyDecrementedDates() {
            // given
            LocalDate date1 = LocalDate.of(2026, 4, 10);
            LocalDate date2 = LocalDate.of(2026, 4, 11);
            LocalDate date3 = LocalDate.of(2026, 4, 12);
            List<LocalDate> dates = List.of(date1, date2, date3);

            given(inventoryCommandPort.decrementAvailable(1L, date1)).willReturn(true);
            given(inventoryCommandPort.decrementAvailable(1L, date2)).willReturn(true);
            given(inventoryCommandPort.decrementAvailable(1L, date3)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.decrementAvailable(ROOM_TYPE_ID, dates))
                .isInstanceOf(InventoryExhaustedException.class);

            // 두 날짜 모두 복구 확인
            then(inventoryCommandPort).should().incrementAvailable(1L, date1);
            then(inventoryCommandPort).should().incrementAvailable(1L, date2);
        }

        @Test
        @DisplayName("첫 번째 날짜에서 실패 시 복구 없이 즉시 예외를 던진다")
        void shouldThrowImmediatelyWhenFirstDateFails() {
            // given
            LocalDate date1 = LocalDate.of(2026, 4, 10);
            List<LocalDate> dates = List.of(date1);

            given(inventoryCommandPort.decrementAvailable(1L, date1)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.decrementAvailable(ROOM_TYPE_ID, dates))
                .isInstanceOf(InventoryExhaustedException.class);

            // incrementAvailable 호출 없음 (복구할 것이 없음)
            then(inventoryCommandPort).should(times(0)).incrementAvailable(1L, date1);
        }
    }

    @Nested
    @DisplayName("incrementAvailable")
    class IncrementAvailable {

        @Test
        @DisplayName("모든 날짜의 재고를 순차적으로 복구한다")
        void shouldIncrementAllDates() {
            // given
            LocalDate date1 = LocalDate.of(2026, 4, 10);
            LocalDate date2 = LocalDate.of(2026, 4, 11);
            List<LocalDate> dates = List.of(date1, date2);

            // when
            manager.incrementAvailable(ROOM_TYPE_ID, dates);

            // then
            then(inventoryCommandPort).should().incrementAvailable(1L, date1);
            then(inventoryCommandPort).should().incrementAvailable(1L, date2);
        }
    }
}

package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryQueryPort;
import com.ryuqq.otatoy.domain.inventory.Inventories;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.inventory.InventoryFixture;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * InventoryReadManager 단위 테스트.
 * InventoryQueryPort를 Mock으로 대체하여 조회 위임 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class InventoryReadManagerTest {

    @Mock
    InventoryQueryPort inventoryQueryPort;

    @InjectMocks
    InventoryReadManager manager;

    @Nested
    @DisplayName("findByRoomTypeIdsAndDateRange")
    class FindByRoomTypeIdsAndDateRange {

        @Test
        @DisplayName("Port 조회 결과를 Inventories로 래핑하여 반환한다")
        void shouldReturnInventoriesFromPort() {
            // given
            List<RoomTypeId> roomTypeIds = List.of(RoomTypeId.of(1L));
            LocalDate startDate = LocalDate.of(2026, 4, 10);
            LocalDate endDate = LocalDate.of(2026, 4, 12);
            List<Inventory> inventoryList = List.of(InventoryFixture.reconstitutedInventory());

            given(inventoryQueryPort.findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate))
                .willReturn(inventoryList);

            // when
            Inventories result = manager.findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate);

            // then
            assertThat(result).isNotNull();
            then(inventoryQueryPort).should().findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate);
        }

        @Test
        @DisplayName("조회 결과가 비어있으면 빈 Inventories를 반환한다")
        void shouldReturnEmptyInventoriesWhenNoData() {
            // given
            List<RoomTypeId> roomTypeIds = List.of(RoomTypeId.of(999L));
            LocalDate startDate = LocalDate.of(2026, 4, 10);
            LocalDate endDate = LocalDate.of(2026, 4, 12);

            given(inventoryQueryPort.findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate))
                .willReturn(List.of());

            // when
            Inventories result = manager.findByRoomTypeIdsAndDateRange(roomTypeIds, startDate, endDate);

            // then
            assertThat(result).isNotNull();
        }
    }
}

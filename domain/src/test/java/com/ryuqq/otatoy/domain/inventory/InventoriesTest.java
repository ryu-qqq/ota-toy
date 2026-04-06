package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InventoriesTest {

    // === 기본 상수 ===
    private static final RoomTypeId ROOM_TYPE_1 = RoomTypeId.of(1L);
    private static final RoomTypeId ROOM_TYPE_2 = RoomTypeId.of(2L);
    private static final LocalDate DATE_1 = LocalDate.of(2026, 4, 10);
    private static final LocalDate DATE_2 = LocalDate.of(2026, 4, 11);
    private static final LocalDate DATE_3 = LocalDate.of(2026, 4, 12);

    @Nested
    @DisplayName("생성 검증 -- of()")
    class Creation {

        @Test
        @DisplayName("null 리스트로 생성하면 빈 컬렉션이 된다")
        void shouldCreateEmptyWhenNull() {
            Inventories inventories = Inventories.from(null);

            assertThat(inventories.isEmpty()).isTrue();
            assertThat(inventories.size()).isZero();
            assertThat(inventories.items()).isEmpty();
        }

        @Test
        @DisplayName("빈 리스트로 생성하면 빈 컬렉션이 된다")
        void shouldCreateEmptyWhenEmptyList() {
            Inventories inventories = Inventories.from(List.of());

            assertThat(inventories.isEmpty()).isTrue();
            assertThat(inventories.size()).isZero();
        }

        @Test
        @DisplayName("재고 목록으로 생성하면 동일한 항목을 포함한다")
        void shouldCreateWithItems() {
            Inventory inv1 = InventoryFixture.inventoryForDate(DATE_1);
            Inventory inv2 = InventoryFixture.inventoryForDate(DATE_2);

            Inventories inventories = Inventories.from(List.of(inv1, inv2));

            assertThat(inventories.size()).isEqualTo(2);
            assertThat(inventories.isEmpty()).isFalse();
            assertThat(inventories.items()).containsExactly(inv1, inv2);
        }

        @Test
        @DisplayName("내부 리스트는 불변이다 (원본 수정해도 영향 없음)")
        void shouldBeImmutable() {
            Inventory inv1 = InventoryFixture.inventoryForDate(DATE_1);
            List<Inventory> original = new java.util.ArrayList<>();
            original.add(inv1);

            Inventories inventories = Inventories.from(original);
            original.clear(); // 원본 비우기

            assertThat(inventories.size()).isEqualTo(1);
            assertThat(inventories.items()).containsExactly(inv1);
        }
    }

    @Nested
    @DisplayName("그룹핑 검증 -- groupByRoomTypeAndDate()")
    class GroupByRoomTypeAndDate {

        @Test
        @DisplayName("빈 컬렉션이면 빈 맵을 반환한다")
        void shouldReturnEmptyMapWhenEmpty() {
            Inventories inventories = Inventories.from(List.of());

            Map<RoomTypeId, Map<LocalDate, Inventory>> grouped = inventories.groupByRoomTypeAndDate();

            assertThat(grouped).isEmpty();
        }

        @Test
        @DisplayName("같은 RoomType의 여러 날짜 재고를 올바르게 그룹핑한다")
        void shouldGroupByDateForSameRoomType() {
            Inventory inv1 = InventoryFixture.reconstituted(1L, 1L, DATE_1, 10, 10, false, 1);
            Inventory inv2 = InventoryFixture.reconstituted(2L, 1L, DATE_2, 10, 8, false, 1);
            Inventory inv3 = InventoryFixture.reconstituted(3L, 1L, DATE_3, 10, 5, false, 1);

            Inventories inventories = Inventories.from(List.of(inv1, inv2, inv3));
            Map<RoomTypeId, Map<LocalDate, Inventory>> grouped = inventories.groupByRoomTypeAndDate();

            assertThat(grouped).hasSize(1);
            assertThat(grouped).containsKey(ROOM_TYPE_1);

            Map<LocalDate, Inventory> dateMap = grouped.get(ROOM_TYPE_1);
            assertThat(dateMap).hasSize(3);
            assertThat(dateMap.get(DATE_1)).isEqualTo(inv1);
            assertThat(dateMap.get(DATE_2)).isEqualTo(inv2);
            assertThat(dateMap.get(DATE_3)).isEqualTo(inv3);
        }

        @Test
        @DisplayName("여러 RoomType의 재고를 올바르게 그룹핑한다")
        void shouldGroupByMultipleRoomTypes() {
            Inventory inv1 = InventoryFixture.reconstituted(1L, 1L, DATE_1, 10, 10, false, 1);
            Inventory inv2 = InventoryFixture.reconstituted(2L, 1L, DATE_2, 10, 8, false, 1);
            Inventory inv3 = InventoryFixture.reconstituted(3L, 2L, DATE_1, 5, 5, false, 1);
            Inventory inv4 = InventoryFixture.reconstituted(4L, 2L, DATE_2, 5, 3, false, 1);

            Inventories inventories = Inventories.from(List.of(inv1, inv2, inv3, inv4));
            Map<RoomTypeId, Map<LocalDate, Inventory>> grouped = inventories.groupByRoomTypeAndDate();

            assertThat(grouped).hasSize(2);
            assertThat(grouped.get(ROOM_TYPE_1)).hasSize(2);
            assertThat(grouped.get(ROOM_TYPE_2)).hasSize(2);
            assertThat(grouped.get(ROOM_TYPE_1).get(DATE_1)).isEqualTo(inv1);
            assertThat(grouped.get(ROOM_TYPE_2).get(DATE_1)).isEqualTo(inv3);
        }

        @Test
        @DisplayName("단일 재고도 올바르게 그룹핑된다")
        void shouldGroupSingleItem() {
            Inventory inv = InventoryFixture.reconstituted(1L, 1L, DATE_1, 10, 10, false, 1);

            Inventories inventories = Inventories.from(List.of(inv));
            Map<RoomTypeId, Map<LocalDate, Inventory>> grouped = inventories.groupByRoomTypeAndDate();

            assertThat(grouped).hasSize(1);
            assertThat(grouped.get(ROOM_TYPE_1)).hasSize(1);
            assertThat(grouped.get(ROOM_TYPE_1).get(DATE_1)).isEqualTo(inv);
        }
    }
}

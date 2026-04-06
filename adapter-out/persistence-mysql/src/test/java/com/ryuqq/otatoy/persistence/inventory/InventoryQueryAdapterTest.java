package com.ryuqq.otatoy.persistence.inventory;

import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.inventory.adapter.InventoryCommandAdapter;
import com.ryuqq.otatoy.persistence.inventory.adapter.InventoryQueryAdapter;
import com.ryuqq.otatoy.persistence.inventory.mapper.InventoryEntityMapper;
import com.ryuqq.otatoy.persistence.inventory.repository.InventoryQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inventory Query Adapter 통합 테스트.
 * RoomTypeId + 날짜 범위 기반 조회를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        InventoryCommandAdapter.class,
        InventoryQueryAdapter.class,
        InventoryEntityMapper.class,
        InventoryQueryDslRepository.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class
})
class InventoryQueryAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private InventoryCommandAdapter inventoryCommandAdapter;

    @Autowired
    private InventoryQueryAdapter inventoryQueryAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    private Long roomTypeId;

    @BeforeEach
    void setUp() {
        Long propId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("재고 테스트 호텔"));
        roomTypeId = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(propId), RoomTypeName.of("테스트 룸"),
                        null, null, null, 2, 4, 5, null, null, Instant.now()));

        // 4/10 ~ 4/14 5일치 Inventory 생성
        List<Inventory> inventories = new java.util.ArrayList<>();
        for (int i = 10; i <= 14; i++) {
            inventories.add(Inventory.forNew(
                    RoomTypeId.of(roomTypeId),
                    LocalDate.of(2026, 4, i),
                    5, Instant.now()
            ));
        }
        inventoryCommandAdapter.persistAll(inventories);
    }

    @Test
    @DisplayName("날짜 범위 내 Inventory만 조회된다 (startDate 포함, endDate 미포함)")
    void shouldFindInventoryByDateRange() {
        // when - 4/11 ~ 4/13 (3일치)
        List<Inventory> inventories = inventoryQueryAdapter.findByRoomTypeIdsAndDateRange(
                List.of(RoomTypeId.of(roomTypeId)),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 14)
        );

        // then
        assertThat(inventories).hasSize(3);
        assertThat(inventories).allMatch(inv -> !inv.inventoryDate().isBefore(LocalDate.of(2026, 4, 11)));
        assertThat(inventories).allMatch(inv -> inv.inventoryDate().isBefore(LocalDate.of(2026, 4, 14)));
    }

    @Test
    @DisplayName("존재하지 않는 RoomTypeId로 조회 시 빈 리스트를 반환한다")
    void shouldReturnEmptyForNonExistingRoomTypeId() {
        List<Inventory> inventories = inventoryQueryAdapter.findByRoomTypeIdsAndDateRange(
                List.of(RoomTypeId.of(99999L)),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 15)
        );
        assertThat(inventories).isEmpty();
    }

    @Test
    @DisplayName("Inventory의 모든 필드가 정합성을 유지한다")
    void shouldMapAllFieldsCorrectly() {
        List<Inventory> inventories = inventoryQueryAdapter.findByRoomTypeIdsAndDateRange(
                List.of(RoomTypeId.of(roomTypeId)),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 11)
        );

        assertThat(inventories).hasSize(1);
        Inventory result = inventories.getFirst();
        assertThat(result.id()).isNotNull();
        assertThat(result.roomTypeId().value()).isEqualTo(roomTypeId);
        assertThat(result.inventoryDate()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(result.totalInventory()).isEqualTo(5);
        assertThat(result.availableCount()).isEqualTo(5);
        assertThat(result.isStopSell()).isFalse();
    }

    @Test
    @DisplayName("여러 RoomTypeId로 동시 조회가 가능하다")
    void shouldFindByMultipleRoomTypeIds() {
        // given - 추가 RoomType과 Inventory 생성
        Long propId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("추가 호텔"));
        Long roomTypeId2 = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(propId), RoomTypeName.of("추가 룸"),
                        null, null, null, 1, 2, 3, null, null, Instant.now()));
        inventoryCommandAdapter.persistAll(List.of(
                Inventory.forNew(RoomTypeId.of(roomTypeId2), LocalDate.of(2026, 4, 10), 3, Instant.now())));

        // when
        List<Inventory> inventories = inventoryQueryAdapter.findByRoomTypeIdsAndDateRange(
                List.of(RoomTypeId.of(roomTypeId), RoomTypeId.of(roomTypeId2)),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 11)
        );

        // then
        assertThat(inventories).hasSize(2);
    }
}

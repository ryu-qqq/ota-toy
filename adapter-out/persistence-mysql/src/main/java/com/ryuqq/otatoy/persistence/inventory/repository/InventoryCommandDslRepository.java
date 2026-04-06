package com.ryuqq.otatoy.persistence.inventory.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.inventory.entity.QInventoryJpaEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Inventory 원자적 쓰기 전용 QueryDsl Repository.
 * DB 제약 조건 방식으로 재고를 차감한다 (ADR-001).
 *
 * UPDATE inventory SET available_count = available_count - 1
 * WHERE room_type_id = ? AND inventory_date = ? AND available_count >= 1
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class InventoryCommandDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QInventoryJpaEntity inventory = QInventoryJpaEntity.inventoryJpaEntity;

    public InventoryCommandDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 특정 날짜의 재고를 원자적으로 1 차감한다.
     * available_count >= 1 조건이 충족되지 않으면 0행 영향 (실패).
     *
     * @return 영향 받은 행 수 (1: 성공, 0: 재고 부족)
     */
    public long decrementAvailable(Long roomTypeId, LocalDate date) {
        return queryFactory
            .update(inventory)
            .set(inventory.availableCount, inventory.availableCount.subtract(1))
            .where(
                inventory.roomTypeId.eq(roomTypeId),
                inventory.inventoryDate.eq(date),
                inventory.availableCount.goe(1),
                inventory.stopSell.isFalse(),
                inventory.deleted.isFalse()
            )
            .execute();
    }

    /**
     * 특정 날짜의 재고를 원자적으로 1 복구한다.
     * 예약 취소 시 사용.
     *
     * @return 영향 받은 행 수
     */
    public long incrementAvailable(Long roomTypeId, LocalDate date) {
        return queryFactory
            .update(inventory)
            .set(inventory.availableCount, inventory.availableCount.add(1))
            .where(
                inventory.roomTypeId.eq(roomTypeId),
                inventory.inventoryDate.eq(date),
                inventory.deleted.isFalse()
            )
            .execute();
    }
}

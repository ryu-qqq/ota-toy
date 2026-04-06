package com.ryuqq.otatoy.persistence.inventory.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import com.ryuqq.otatoy.persistence.inventory.entity.QInventoryJpaEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Inventory 조회 ���용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class InventoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QInventoryJpaEntity inventory = QInventoryJpaEntity.inventoryJpaEntity;

    public InventoryQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 특정 객실 유형들의 날짜 범위에 해당하는 재고 목���을 조회한다.
     * startDate 포함, endDate 미포함.
     */
    public List<InventoryJpaEntity> findByRoomTypeIdsAndDateRange(List<Long> roomTypeIds,
                                                                   LocalDate startDate,
                                                                   LocalDate endDate) {
        return queryFactory
                .selectFrom(inventory)
                .where(
                        inventory.roomTypeId.in(roomTypeIds),
                        inventory.inventoryDate.goe(startDate),
                        inventory.inventoryDate.lt(endDate),
                        inventory.deleted.isFalse()
                )
                .fetch();
    }
}

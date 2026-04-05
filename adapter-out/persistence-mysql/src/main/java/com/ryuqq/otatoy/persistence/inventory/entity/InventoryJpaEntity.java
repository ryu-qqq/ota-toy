package com.ryuqq.otatoy.persistence.inventory.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Inventory JPA Entity.
 * 날짜별 객실 재고 데이터를 매핑하는 순수 데이터 매핑 객체.
 * {@code @Version}으로 낙관적 락을 지원한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "inventory")
public class InventoryJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate inventoryDate;

    @Column(nullable = false)
    private int totalInventory;

    @Column(nullable = false)
    private int availableCount;

    @Column(name = "is_stop_sell", nullable = false)
    private boolean stopSell;

    @Version
    @Column(nullable = false)
    private int version;

    protected InventoryJpaEntity() {
        super();
    }

    private InventoryJpaEntity(Long id, Long roomTypeId, LocalDate inventoryDate,
                                int totalInventory, int availableCount, boolean stopSell, int version,
                                Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.inventoryDate = inventoryDate;
        this.totalInventory = totalInventory;
        this.availableCount = availableCount;
        this.stopSell = stopSell;
        this.version = version;
    }

    public static InventoryJpaEntity create(Long id, Long roomTypeId, LocalDate inventoryDate,
                                             int totalInventory, int availableCount, boolean stopSell, int version,
                                             Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new InventoryJpaEntity(id, roomTypeId, inventoryDate,
                totalInventory, availableCount, stopSell, version,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public LocalDate getInventoryDate() { return inventoryDate; }
    public int getTotalInventory() { return totalInventory; }
    public int getAvailableCount() { return availableCount; }
    public boolean isStopSell() { return stopSell; }
    public int getVersion() { return version; }
}

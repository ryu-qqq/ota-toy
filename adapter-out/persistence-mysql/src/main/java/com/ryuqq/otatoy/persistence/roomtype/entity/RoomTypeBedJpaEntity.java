package com.ryuqq.otatoy.persistence.roomtype.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * RoomTypeBed JPA Entity.
 * 객실 침대 구성을 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "room_type_bed")
public class RoomTypeBedJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private Long bedTypeId;

    @Column(nullable = false)
    private int quantity;

    protected RoomTypeBedJpaEntity() {
        super();
    }

    private RoomTypeBedJpaEntity(Long id, Long roomTypeId, Long bedTypeId, int quantity,
                                  Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.bedTypeId = bedTypeId;
        this.quantity = quantity;
    }

    public static RoomTypeBedJpaEntity create(Long id, Long roomTypeId, Long bedTypeId, int quantity,
                                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RoomTypeBedJpaEntity(id, roomTypeId, bedTypeId, quantity,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public Long getBedTypeId() { return bedTypeId; }
    public int getQuantity() { return quantity; }
}

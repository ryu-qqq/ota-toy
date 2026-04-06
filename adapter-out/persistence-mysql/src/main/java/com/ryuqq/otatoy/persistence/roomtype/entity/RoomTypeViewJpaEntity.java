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
 * RoomTypeView JPA Entity.
 * 객실 전망 유형 매핑을 위한 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "room_type_view")
public class RoomTypeViewJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private Long viewTypeId;

    protected RoomTypeViewJpaEntity() {
        super();
    }

    private RoomTypeViewJpaEntity(Long id, Long roomTypeId, Long viewTypeId,
                                   Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.viewTypeId = viewTypeId;
    }

    public static RoomTypeViewJpaEntity create(Long id, Long roomTypeId, Long viewTypeId,
                                                Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RoomTypeViewJpaEntity(id, roomTypeId, viewTypeId,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public Long getViewTypeId() { return viewTypeId; }
}

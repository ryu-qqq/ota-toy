package com.ryuqq.otatoy.persistence.roomtype.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * RoomType JPA Entity.
 * 객실 유형 기본 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "room_type")
public class RoomTypeJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 6, scale = 2)
    private BigDecimal areaSqm;

    @Column(length = 20)
    private String areaPyeong;

    @Column(nullable = false)
    private int baseOccupancy;

    @Column(nullable = false)
    private int maxOccupancy;

    @Column(nullable = false)
    private int baseInventory;

    @Column(length = 10)
    private String checkInTime;

    @Column(length = 10)
    private String checkOutTime;

    @Column(nullable = false, length = 30)
    private String status;

    protected RoomTypeJpaEntity() {
        super();
    }

    private RoomTypeJpaEntity(Long id, Long propertyId, String name, String description,
                               BigDecimal areaSqm, String areaPyeong,
                               int baseOccupancy, int maxOccupancy, int baseInventory,
                               String checkInTime, String checkOutTime, String status,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyId = propertyId;
        this.name = name;
        this.description = description;
        this.areaSqm = areaSqm;
        this.areaPyeong = areaPyeong;
        this.baseOccupancy = baseOccupancy;
        this.maxOccupancy = maxOccupancy;
        this.baseInventory = baseInventory;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
    }

    public static RoomTypeJpaEntity create(Long id, Long propertyId, String name, String description,
                                            BigDecimal areaSqm, String areaPyeong,
                                            int baseOccupancy, int maxOccupancy, int baseInventory,
                                            String checkInTime, String checkOutTime, String status,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RoomTypeJpaEntity(id, propertyId, name, description,
                areaSqm, areaPyeong, baseOccupancy, maxOccupancy, baseInventory,
                checkInTime, checkOutTime, status,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getAreaSqm() { return areaSqm; }
    public String getAreaPyeong() { return areaPyeong; }
    public int getBaseOccupancy() { return baseOccupancy; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public int getBaseInventory() { return baseInventory; }
    public String getCheckInTime() { return checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public String getStatus() { return status; }
}

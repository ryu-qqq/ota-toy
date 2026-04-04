package com.ryuqq.otatoy.domain.accommodation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

public class RoomType {

    private final RoomTypeId id;
    private final PropertyId propertyId;
    private String name;
    private String description;
    private BigDecimal areaSqm;
    private String areaPyeong;
    private int baseOccupancy;
    private int maxOccupancy;
    private int baseInventory;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private RoomTypeStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private RoomType(RoomTypeId id, PropertyId propertyId, String name, String description,
                     BigDecimal areaSqm, String areaPyeong, int baseOccupancy, int maxOccupancy,
                     int baseInventory, LocalTime checkInTime, LocalTime checkOutTime,
                     RoomTypeStatus status, Instant createdAt, Instant updatedAt) {
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RoomType forNew(PropertyId propertyId, String name, String description,
                                   BigDecimal areaSqm, String areaPyeong,
                                   int baseOccupancy, int maxOccupancy, int baseInventory,
                                   LocalTime checkInTime, LocalTime checkOutTime, Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("객실명은 필수입니다");
        }
        if (baseOccupancy <= 0) {
            throw new IllegalArgumentException("기본 인원은 1명 이상이어야 합니다");
        }
        if (maxOccupancy < baseOccupancy) {
            throw new IllegalArgumentException("최대 인원은 기본 인원 이상이어야 합니다");
        }
        if (baseInventory < 0) {
            throw new IllegalArgumentException("기본 재고는 0 이상이어야 합니다");
        }
        if (areaSqm != null && areaSqm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("객실 면적은 0보다 커야 합니다");
        }
        return new RoomType(null, propertyId, name, description, areaSqm, areaPyeong,
                baseOccupancy, maxOccupancy, baseInventory, checkInTime, checkOutTime,
                RoomTypeStatus.ACTIVE, now, now);
    }

    public static RoomType reconstitute(RoomTypeId id, PropertyId propertyId, String name, String description,
                                         BigDecimal areaSqm, String areaPyeong,
                                         int baseOccupancy, int maxOccupancy, int baseInventory,
                                         LocalTime checkInTime, LocalTime checkOutTime,
                                         RoomTypeStatus status, Instant createdAt, Instant updatedAt) {
        return new RoomType(id, propertyId, name, description, areaSqm, areaPyeong,
                baseOccupancy, maxOccupancy, baseInventory, checkInTime, checkOutTime,
                status, createdAt, updatedAt);
    }

    public void updateInfo(String name, String description, BigDecimal areaSqm, String areaPyeong,
                           int baseOccupancy, int maxOccupancy, Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("객실명은 필수입니다");
        }
        if (maxOccupancy < baseOccupancy) {
            throw new IllegalArgumentException("최대 인원은 기본 인원 이상이어야 합니다");
        }
        if (areaSqm != null && areaSqm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("객실 면적은 0보다 커야 합니다");
        }
        this.name = name;
        this.description = description;
        this.areaSqm = areaSqm;
        this.areaPyeong = areaPyeong;
        this.baseOccupancy = baseOccupancy;
        this.maxOccupancy = maxOccupancy;
        this.updatedAt = now;
    }

    public void updateInventory(int baseInventory, Instant now) {
        if (baseInventory < 0) {
            throw new IllegalArgumentException("기본 재고는 0 이상이어야 합니다");
        }
        this.baseInventory = baseInventory;
        this.updatedAt = now;
    }

    public void updateCheckInOut(LocalTime checkInTime, LocalTime checkOutTime, Instant now) {
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        this.status = RoomTypeStatus.INACTIVE;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = RoomTypeStatus.ACTIVE;
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == RoomTypeStatus.ACTIVE;
    }

    public RoomTypeId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public String name() { return name; }
    public String description() { return description; }
    public BigDecimal areaSqm() { return areaSqm; }
    public String areaPyeong() { return areaPyeong; }
    public int baseOccupancy() { return baseOccupancy; }
    public int maxOccupancy() { return maxOccupancy; }
    public int baseInventory() { return baseInventory; }
    public LocalTime checkInTime() { return checkInTime; }
    public LocalTime checkOutTime() { return checkOutTime; }
    public RoomTypeStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomType r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

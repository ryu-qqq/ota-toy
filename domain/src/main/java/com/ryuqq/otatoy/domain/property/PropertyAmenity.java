package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;

import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * 숙소에 연결된 개별 편의시설을 나타내는 엔티티.
 * 편의시설 유형, 이름, 추가 요금, 정렬 순서를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PropertyAmenity {

    private final PropertyAmenityId id;
    private final PropertyId propertyId;
    private final AmenityType amenityType;
    private final AmenityName name;
    private final Money additionalPrice;
    private final int sortOrder;
    private final Instant createdAt;
    private Instant updatedAt;
    private DeletionStatus deletionStatus;

    private PropertyAmenity(PropertyAmenityId id, PropertyId propertyId, AmenityType amenityType,
                            AmenityName name, Money additionalPrice, int sortOrder,
                            Instant createdAt, Instant updatedAt, DeletionStatus deletionStatus) {
        this.id = id;
        this.propertyId = propertyId;
        this.amenityType = amenityType;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletionStatus = deletionStatus;
    }

    public static PropertyAmenity forNew(PropertyId propertyId, AmenityType amenityType, AmenityName name,
                                          Money additionalPrice, int sortOrder, Instant now) {
        validate(amenityType);
        return new PropertyAmenity(PropertyAmenityId.of(null), propertyId, amenityType, name,
                additionalPrice, sortOrder, now, now, DeletionStatus.active());
    }

    private static void validate(AmenityType amenityType) {
        if (amenityType == null) {
            throw new IllegalArgumentException("편의시설 유형은 필수입니다");
        }
    }

    public static PropertyAmenity reconstitute(PropertyAmenityId id, PropertyId propertyId, AmenityType amenityType,
                                                AmenityName name, Money additionalPrice, int sortOrder,
                                                Instant createdAt, Instant updatedAt, DeletionStatus deletionStatus) {
        return new PropertyAmenity(id, propertyId, amenityType, name, additionalPrice, sortOrder,
                createdAt, updatedAt, deletionStatus);
    }

    /**
     * soft delete 처리한다. 이미 삭제된 상태이면 무시한다.
     */
    public void delete(Instant now) {
        if (!deletionStatus.deleted()) {
            this.deletionStatus = DeletionStatus.deleted(now);
            this.updatedAt = now;
        }
    }

    /**
     * diff 비교를 위한 비즈니스 키. amenityType + name 조합으로 동일 편의시설을 식별한다.
     */
    public String amenityKey() {
        return amenityType.name() + ":" + name.value();
    }

    public boolean isFree() {
        return additionalPrice == null || additionalPrice.isZero();
    }

    public boolean isDeleted() {
        return deletionStatus.deleted();
    }

    public PropertyAmenityId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public AmenityType amenityType() { return amenityType; }
    public AmenityName name() { return name; }
    public Money additionalPrice() { return additionalPrice; }
    public int sortOrder() { return sortOrder; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public DeletionStatus deletionStatus() { return deletionStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAmenity p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

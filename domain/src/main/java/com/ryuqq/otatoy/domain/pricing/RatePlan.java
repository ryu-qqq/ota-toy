package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.time.Instant;
import java.util.Objects;

/**
 * 요금 정책(Rate Plan)을 나타내는 Aggregate Root.
 * 객실 유형에 연결되며, 취소 정책, 결제 정책, 공급 소스를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 * @see RateRule 요금 규칙
 * @see RatePlanAddOn 부가 서비스
 * @see Rate 날짜별 최종 요금
 */
public class RatePlan {

    private final RatePlanId id;
    private final RoomTypeId roomTypeId;
    private RatePlanName name;
    private SourceType sourceType;
    private SupplierId supplierId;
    private CancellationPolicy cancellationPolicy;
    private PaymentPolicy paymentPolicy;
    private final Instant createdAt;
    private Instant updatedAt;

    private RatePlan(RatePlanId id, RoomTypeId roomTypeId, RatePlanName name,
                     SourceType sourceType, SupplierId supplierId,
                     CancellationPolicy cancellationPolicy, PaymentPolicy paymentPolicy,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.name = name;
        this.sourceType = sourceType;
        this.supplierId = supplierId;
        this.cancellationPolicy = cancellationPolicy;
        this.paymentPolicy = paymentPolicy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RatePlan forNew(RoomTypeId roomTypeId, RatePlanName name,
                                   SourceType sourceType, SupplierId supplierId,
                                   CancellationPolicy cancellationPolicy, PaymentPolicy paymentPolicy,
                                   Instant now) {
        validateRequired(roomTypeId, paymentPolicy);
        validateSupplier(sourceType, supplierId);
        return new RatePlan(null, roomTypeId, name, sourceType, supplierId,
                cancellationPolicy, paymentPolicy, now, now);
    }

    public static RatePlan reconstitute(RatePlanId id, RoomTypeId roomTypeId, RatePlanName name,
                                         SourceType sourceType, SupplierId supplierId,
                                         CancellationPolicy cancellationPolicy, PaymentPolicy paymentPolicy,
                                         Instant createdAt, Instant updatedAt) {
        return new RatePlan(id, roomTypeId, name, sourceType, supplierId,
                cancellationPolicy, paymentPolicy, createdAt, updatedAt);
    }

    public void updatePolicy(CancellationPolicy cancellationPolicy, PaymentPolicy paymentPolicy, Instant now) {
        this.cancellationPolicy = cancellationPolicy;
        this.paymentPolicy = paymentPolicy;
        this.updatedAt = now;
    }

    public void rename(RatePlanName newName, Instant now) {
        this.name = newName;
        this.updatedAt = now;
    }

    private static void validateRequired(RoomTypeId roomTypeId, PaymentPolicy paymentPolicy) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (paymentPolicy == null) {
            throw new IllegalArgumentException("결제 방식은 필수입니다");
        }
    }

    private static void validateSupplier(SourceType sourceType, SupplierId supplierId) {
        if (sourceType == SourceType.SUPPLIER && supplierId == null) {
            throw new IllegalArgumentException("외부 공급자 요금 정책은 supplierId가 필수입니다");
        }
    }

    public boolean isDirect() {
        return sourceType == SourceType.DIRECT;
    }

    public boolean isSupplier() {
        return sourceType == SourceType.SUPPLIER;
    }

    public RatePlanId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public RatePlanName name() { return name; }
    public SourceType sourceType() { return sourceType; }
    public SupplierId supplierId() { return supplierId; }
    public CancellationPolicy cancellationPolicy() { return cancellationPolicy; }
    public PaymentPolicy paymentPolicy() { return paymentPolicy; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RatePlan r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

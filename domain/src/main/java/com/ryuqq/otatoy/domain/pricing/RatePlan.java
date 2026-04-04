package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;

import java.time.Instant;
import java.util.Objects;

public class RatePlan {

    private final RatePlanId id;
    private final RoomTypeId roomTypeId;
    private String name;
    private SourceType sourceType;
    private Long supplierId;
    private boolean freeCancellation;
    private boolean nonRefundable;
    private String cancellationPolicyText;
    private PaymentPolicy paymentPolicy;
    private final Instant createdAt;
    private Instant updatedAt;

    private RatePlan(RatePlanId id, RoomTypeId roomTypeId, String name,
                     SourceType sourceType, Long supplierId,
                     boolean freeCancellation, boolean nonRefundable,
                     String cancellationPolicyText, PaymentPolicy paymentPolicy,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.name = name;
        this.sourceType = sourceType;
        this.supplierId = supplierId;
        this.freeCancellation = freeCancellation;
        this.nonRefundable = nonRefundable;
        this.cancellationPolicyText = cancellationPolicyText;
        this.paymentPolicy = paymentPolicy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RatePlan forNew(RoomTypeId roomTypeId, String name,
                                   SourceType sourceType, Long supplierId,
                                   boolean freeCancellation, boolean nonRefundable,
                                   String cancellationPolicyText, PaymentPolicy paymentPolicy,
                                   Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("요금 정책명은 필수입니다");
        }
        if (paymentPolicy == null) {
            throw new IllegalArgumentException("결제 방식은 필수입니다");
        }
        if (sourceType == SourceType.SUPPLIER && supplierId == null) {
            throw new IllegalArgumentException("외부 공급자 요금 정책은 supplierId가 필수입니다");
        }
        return new RatePlan(null, roomTypeId, name, sourceType, supplierId,
                freeCancellation, nonRefundable, cancellationPolicyText, paymentPolicy,
                now, now);
    }

    public static RatePlan reconstitute(RatePlanId id, RoomTypeId roomTypeId, String name,
                                         SourceType sourceType, Long supplierId,
                                         boolean freeCancellation, boolean nonRefundable,
                                         String cancellationPolicyText, PaymentPolicy paymentPolicy,
                                         Instant createdAt, Instant updatedAt) {
        return new RatePlan(id, roomTypeId, name, sourceType, supplierId,
                freeCancellation, nonRefundable, cancellationPolicyText, paymentPolicy,
                createdAt, updatedAt);
    }

    public void updatePolicy(boolean freeCancellation, boolean nonRefundable,
                              String cancellationPolicyText, PaymentPolicy paymentPolicy, Instant now) {
        this.freeCancellation = freeCancellation;
        this.nonRefundable = nonRefundable;
        this.cancellationPolicyText = cancellationPolicyText;
        this.paymentPolicy = paymentPolicy;
        this.updatedAt = now;
    }

    public boolean isDirect() {
        return sourceType == SourceType.DIRECT;
    }

    public boolean isSupplier() {
        return sourceType == SourceType.SUPPLIER;
    }

    public RatePlanId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String name() { return name; }
    public SourceType sourceType() { return sourceType; }
    public Long supplierId() { return supplierId; }
    public boolean freeCancellation() { return freeCancellation; }
    public boolean nonRefundable() { return nonRefundable; }
    public String cancellationPolicyText() { return cancellationPolicyText; }
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

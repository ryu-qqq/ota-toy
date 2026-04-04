package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.time.Instant;
import java.util.Objects;

public class RatePlan {

    private final RatePlanId id;
    private final RoomTypeId roomTypeId;
    private RatePlanName name;
    private SourceType sourceType;
    private SupplierId supplierId;
    private boolean freeCancellation;
    private boolean nonRefundable;
    private int freeCancellationDeadlineDays;
    private String cancellationPolicyText;
    private PaymentPolicy paymentPolicy;
    private final Instant createdAt;
    private Instant updatedAt;

    private RatePlan(RatePlanId id, RoomTypeId roomTypeId, RatePlanName name,
                     SourceType sourceType, SupplierId supplierId,
                     boolean freeCancellation, boolean nonRefundable,
                     int freeCancellationDeadlineDays,
                     String cancellationPolicyText, PaymentPolicy paymentPolicy,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.name = name;
        this.sourceType = sourceType;
        this.supplierId = supplierId;
        this.freeCancellation = freeCancellation;
        this.nonRefundable = nonRefundable;
        this.freeCancellationDeadlineDays = freeCancellationDeadlineDays;
        this.cancellationPolicyText = cancellationPolicyText;
        this.paymentPolicy = paymentPolicy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RatePlan forNew(RoomTypeId roomTypeId, RatePlanName name,
                                   SourceType sourceType, SupplierId supplierId,
                                   boolean freeCancellation, boolean nonRefundable,
                                   int freeCancellationDeadlineDays,
                                   String cancellationPolicyText, PaymentPolicy paymentPolicy,
                                   Instant now) {
        validateRequired(roomTypeId, paymentPolicy);
        validateSupplier(sourceType, supplierId);
        validateCancellationPolicy(freeCancellation, nonRefundable, freeCancellationDeadlineDays);
        return new RatePlan(null, roomTypeId, name, sourceType, supplierId,
                freeCancellation, nonRefundable, freeCancellationDeadlineDays,
                cancellationPolicyText, paymentPolicy, now, now);
    }

    public static RatePlan reconstitute(RatePlanId id, RoomTypeId roomTypeId, RatePlanName name,
                                         SourceType sourceType, SupplierId supplierId,
                                         boolean freeCancellation, boolean nonRefundable,
                                         int freeCancellationDeadlineDays,
                                         String cancellationPolicyText, PaymentPolicy paymentPolicy,
                                         Instant createdAt, Instant updatedAt) {
        return new RatePlan(id, roomTypeId, name, sourceType, supplierId,
                freeCancellation, nonRefundable, freeCancellationDeadlineDays,
                cancellationPolicyText, paymentPolicy, createdAt, updatedAt);
    }

    public void updatePolicy(boolean freeCancellation, boolean nonRefundable,
                              int freeCancellationDeadlineDays,
                              String cancellationPolicyText, PaymentPolicy paymentPolicy, Instant now) {
        validateCancellationPolicy(freeCancellation, nonRefundable, freeCancellationDeadlineDays);
        this.freeCancellation = freeCancellation;
        this.nonRefundable = nonRefundable;
        this.freeCancellationDeadlineDays = freeCancellationDeadlineDays;
        this.cancellationPolicyText = cancellationPolicyText;
        this.paymentPolicy = paymentPolicy;
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

    private static void validateCancellationPolicy(boolean freeCancellation, boolean nonRefundable,
                                                    int freeCancellationDeadlineDays) {
        if (freeCancellation && nonRefundable) {
            throw new IllegalArgumentException("무료 취소와 환불 불가는 동시에 설정할 수 없습니다");
        }
        if (freeCancellationDeadlineDays < 0) {
            throw new IllegalArgumentException("무료 취소 기한은 0 이상이어야 합니다");
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
    public boolean freeCancellation() { return freeCancellation; }
    public boolean nonRefundable() { return nonRefundable; }
    public int freeCancellationDeadlineDays() { return freeCancellationDeadlineDays; }
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

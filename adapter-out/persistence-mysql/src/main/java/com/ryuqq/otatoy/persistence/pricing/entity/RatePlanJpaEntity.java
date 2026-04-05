package com.ryuqq.otatoy.persistence.pricing.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * RatePlan JPA Entity.
 * 요금 정책 기본 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "rate_plan")
public class RatePlanJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String sourceType;

    private Long supplierId;

    @Column(name = "is_free_cancellation", nullable = false)
    private boolean freeCancellation;

    @Column(name = "is_non_refundable", nullable = false)
    private boolean nonRefundable;

    @Column(nullable = false)
    private int freeCancellationDeadlineDays;

    @Column(columnDefinition = "TEXT")
    private String cancellationPolicyText;

    @Column(nullable = false, length = 30)
    private String paymentPolicy;

    protected RatePlanJpaEntity() {
        super();
    }

    private RatePlanJpaEntity(Long id, Long roomTypeId, String name,
                               String sourceType, Long supplierId,
                               boolean freeCancellation, boolean nonRefundable,
                               int freeCancellationDeadlineDays, String cancellationPolicyText,
                               String paymentPolicy,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
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
    }

    public static RatePlanJpaEntity create(Long id, Long roomTypeId, String name,
                                            String sourceType, Long supplierId,
                                            boolean freeCancellation, boolean nonRefundable,
                                            int freeCancellationDeadlineDays, String cancellationPolicyText,
                                            String paymentPolicy,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RatePlanJpaEntity(id, roomTypeId, name,
                sourceType, supplierId,
                freeCancellation, nonRefundable,
                freeCancellationDeadlineDays, cancellationPolicyText,
                paymentPolicy,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public String getName() { return name; }
    public String getSourceType() { return sourceType; }
    public Long getSupplierId() { return supplierId; }
    public boolean isFreeCancellation() { return freeCancellation; }
    public boolean isNonRefundable() { return nonRefundable; }
    public int getFreeCancellationDeadlineDays() { return freeCancellationDeadlineDays; }
    public String getCancellationPolicyText() { return cancellationPolicyText; }
    public String getPaymentPolicy() { return paymentPolicy; }
}

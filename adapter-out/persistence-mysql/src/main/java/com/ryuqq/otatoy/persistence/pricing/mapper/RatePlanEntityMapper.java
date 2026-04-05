package com.ryuqq.otatoy.persistence.pricing.mapper;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.pricing.SourceType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.persistence.pricing.entity.RatePlanJpaEntity;
import org.springframework.stereotype.Component;

/**
 * RatePlan Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RatePlanJpaEntity toEntity(RatePlan domain) {
        CancellationPolicy cp = domain.cancellationPolicy();
        return RatePlanJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.roomTypeId().value(),
                domain.name().value(),
                domain.sourceType().name(),
                domain.supplierId() != null ? domain.supplierId().value() : null,
                cp != null && cp.freeCancellation(),
                cp != null && cp.nonRefundable(),
                cp != null ? cp.deadlineDays() : 0,
                cp != null ? cp.policyText() : null,
                domain.paymentPolicy().name(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RatePlan toDomain(RatePlanJpaEntity entity) {
        return RatePlan.reconstitute(
                RatePlanId.of(entity.getId()),
                RoomTypeId.of(entity.getRoomTypeId()),
                RatePlanName.of(entity.getName()),
                SourceType.valueOf(entity.getSourceType()),
                entity.getSupplierId() != null ? SupplierId.of(entity.getSupplierId()) : null,
                CancellationPolicy.of(
                        entity.isFreeCancellation(),
                        entity.isNonRefundable(),
                        entity.getFreeCancellationDeadlineDays(),
                        entity.getCancellationPolicyText()
                ),
                PaymentPolicy.valueOf(entity.getPaymentPolicy()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

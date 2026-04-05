package com.ryuqq.otatoy.persistence.pricing.mapper;

import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RateId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.persistence.pricing.entity.RateJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Rate Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RateJpaEntity toEntity(Rate domain) {
        return RateJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.ratePlanId().value(),
                domain.rateDate(),
                domain.basePrice(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public Rate toDomain(RateJpaEntity entity) {
        return Rate.reconstitute(
                RateId.of(entity.getId()),
                RatePlanId.of(entity.getRatePlanId()),
                entity.getRateDate(),
                entity.getBasePrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

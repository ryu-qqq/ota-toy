package com.ryuqq.otatoy.persistence.pricing.mapper;

import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.domain.pricing.RateOverrideId;
import com.ryuqq.otatoy.domain.pricing.RateRuleId;
import com.ryuqq.otatoy.persistence.pricing.entity.RateOverrideJpaEntity;
import org.springframework.stereotype.Component;

/**
 * RateOverride Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateOverrideEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RateOverrideJpaEntity toEntity(RateOverride domain) {
        return RateOverrideJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.rateRuleId() != null ? domain.rateRuleId().value() : null,
                domain.overrideDate(),
                domain.price(),
                domain.reason(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RateOverride toDomain(RateOverrideJpaEntity entity) {
        return RateOverride.reconstitute(
                RateOverrideId.of(entity.getId()),
                RateRuleId.of(entity.getRateRuleId()),
                entity.getOverrideDate(),
                entity.getPrice(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

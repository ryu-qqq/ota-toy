package com.ryuqq.otatoy.persistence.pricing.mapper;

import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RateRule;
import com.ryuqq.otatoy.domain.pricing.RateRuleId;
import com.ryuqq.otatoy.persistence.pricing.entity.RateRuleJpaEntity;
import org.springframework.stereotype.Component;

/**
 * RateRule Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RateRuleEntityMapper {

    /**
     * Domain -> Entity 변환 (저장 시). create() 팩토리 메서드 사용.
     */
    public RateRuleJpaEntity toEntity(RateRule domain) {
        return RateRuleJpaEntity.create(
                domain.id() != null ? domain.id().value() : null,
                domain.ratePlanId().value(),
                domain.startDate(),
                domain.endDate(),
                domain.basePrice(),
                domain.weekdayPrice(),
                domain.fridayPrice(),
                domain.saturdayPrice(),
                domain.sundayPrice(),
                domain.createdAt(),
                domain.updatedAt(),
                null
        );
    }

    /**
     * Entity -> Domain 변환 (조회 시). reconstitute()로 검증 없이 복원.
     */
    public RateRule toDomain(RateRuleJpaEntity entity) {
        return RateRule.reconstitute(
                RateRuleId.of(entity.getId()),
                RatePlanId.of(entity.getRatePlanId()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getBasePrice(),
                entity.getWeekdayPrice(),
                entity.getFridayPrice(),
                entity.getSaturdayPrice(),
                entity.getSundayPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

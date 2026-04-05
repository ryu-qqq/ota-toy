package com.ryuqq.otatoy.persistence.partner.mapper;

import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.partner.PartnerName;
import com.ryuqq.otatoy.domain.partner.PartnerStatus;
import com.ryuqq.otatoy.persistence.partner.entity.PartnerJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Partner Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PartnerEntityMapper {

    public Partner toDomain(PartnerJpaEntity entity) {
        return Partner.reconstitute(
                PartnerId.of(entity.getId()),
                PartnerName.of(entity.getName()),
                PartnerStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

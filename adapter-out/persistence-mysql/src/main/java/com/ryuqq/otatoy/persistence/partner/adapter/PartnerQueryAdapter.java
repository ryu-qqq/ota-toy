package com.ryuqq.otatoy.persistence.partner.adapter;

import com.ryuqq.otatoy.application.partner.port.out.PartnerQueryPort;
import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.persistence.partner.mapper.PartnerEntityMapper;
import com.ryuqq.otatoy.persistence.partner.repository.PartnerQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Partner Query Adapter.
 * PartnerQueryPort를 구현하며, QueryDslRepository만 의존한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PartnerQueryAdapter implements PartnerQueryPort {

    private final PartnerQueryDslRepository queryDslRepository;
    private final PartnerEntityMapper mapper;

    public PartnerQueryAdapter(PartnerQueryDslRepository queryDslRepository, PartnerEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Partner> findById(PartnerId id) {
        return queryDslRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(PartnerId id) {
        return queryDslRepository.existsById(id.value());
    }
}

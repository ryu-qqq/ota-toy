package com.ryuqq.otatoy.application.partner.port.out;

import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;

import java.util.Optional;

/**
 * Partner 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PartnerQueryPort {

    Optional<Partner> findById(PartnerId id);

    boolean existsById(PartnerId id);
}

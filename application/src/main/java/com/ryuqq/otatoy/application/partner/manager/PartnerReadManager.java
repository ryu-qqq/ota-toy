package com.ryuqq.otatoy.application.partner.manager;

import com.ryuqq.otatoy.application.partner.port.out.PartnerQueryPort;
import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Partner 조회 트랜잭션 경계 관리자.
 * 다른 BC에서도 호출 가능한 ReadManager (APP-BC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PartnerReadManager {

    private final PartnerQueryPort partnerQueryPort;

    public PartnerReadManager(PartnerQueryPort partnerQueryPort) {
        this.partnerQueryPort = partnerQueryPort;
    }

    @Transactional(readOnly = true)
    public Partner getById(PartnerId id) {
        return partnerQueryPort.findById(id)
            .orElseThrow(PartnerNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(PartnerId id) {
        if (!partnerQueryPort.existsById(id)) {
            throw new PartnerNotFoundException();
        }
    }
}

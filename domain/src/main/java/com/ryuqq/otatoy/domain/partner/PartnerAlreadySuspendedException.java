package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 이미 정지된 파트너를 다시 정지하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PartnerAlreadySuspendedException extends DomainException {

    public PartnerAlreadySuspendedException() {
        super(PartnerErrorCode.PARTNER_ALREADY_SUSPENDED);
    }
}

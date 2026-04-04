package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

public class PartnerAlreadySuspendedException extends DomainException {

    public PartnerAlreadySuspendedException() {
        super(PartnerErrorCode.PARTNER_ALREADY_SUSPENDED);
    }
}

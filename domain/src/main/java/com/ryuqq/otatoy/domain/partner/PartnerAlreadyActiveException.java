package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

public class PartnerAlreadyActiveException extends DomainException {

    public PartnerAlreadyActiveException() {
        super(PartnerErrorCode.PARTNER_ALREADY_ACTIVE);
    }
}

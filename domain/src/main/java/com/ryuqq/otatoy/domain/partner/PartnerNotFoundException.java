package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

public class PartnerNotFoundException extends DomainException {

    public PartnerNotFoundException() {
        super(PartnerErrorCode.PARTNER_NOT_FOUND);
    }
}

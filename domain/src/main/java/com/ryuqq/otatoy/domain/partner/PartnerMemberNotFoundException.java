package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

public class PartnerMemberNotFoundException extends DomainException {

    public PartnerMemberNotFoundException() {
        super(PartnerErrorCode.PARTNER_MEMBER_NOT_FOUND);
    }
}

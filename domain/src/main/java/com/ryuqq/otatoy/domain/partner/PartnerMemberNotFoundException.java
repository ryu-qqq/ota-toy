package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 파트너 멤버를 찾을 수 없을 때 발생하는 예외.
 */
public class PartnerMemberNotFoundException extends DomainException {

    public PartnerMemberNotFoundException() {
        super(PartnerErrorCode.PARTNER_MEMBER_NOT_FOUND);
    }
}

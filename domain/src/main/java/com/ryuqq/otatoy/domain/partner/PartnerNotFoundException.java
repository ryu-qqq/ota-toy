package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 파트너를 찾을 수 없을 때 발생하는 예외.
 */
public class PartnerNotFoundException extends DomainException {

    public PartnerNotFoundException() {
        super(PartnerErrorCode.PARTNER_NOT_FOUND);
    }
}

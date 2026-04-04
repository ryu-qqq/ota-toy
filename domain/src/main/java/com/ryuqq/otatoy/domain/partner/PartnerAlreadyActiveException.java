package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 이미 활성 상태인 파트너를 다시 활성화하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class PartnerAlreadyActiveException extends DomainException {

    public PartnerAlreadyActiveException() {
        super(PartnerErrorCode.PARTNER_ALREADY_ACTIVE);
    }
}

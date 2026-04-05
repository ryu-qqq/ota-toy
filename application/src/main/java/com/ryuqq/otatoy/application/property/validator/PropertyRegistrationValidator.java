package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.partner.manager.PartnerReadManager;
import com.ryuqq.otatoy.application.propertytype.manager.PropertyTypeReadManager;

import org.springframework.stereotype.Component;

/**
 * 숙소 등록 검증 전용 Validator.
 * ReadManager를 주입받아 파트너/숙소유형 존재 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyRegistrationValidator {

    private final PartnerReadManager partnerReadManager;
    private final PropertyTypeReadManager propertyTypeReadManager;

    public PropertyRegistrationValidator(PartnerReadManager partnerReadManager,
                                          PropertyTypeReadManager propertyTypeReadManager) {
        this.partnerReadManager = partnerReadManager;
        this.propertyTypeReadManager = propertyTypeReadManager;
    }

    /**
     * 숙소 등록 전 파트너/숙소유형 존재 여부를 검증한다.
     * 존재하지 않으면 PartnerNotFoundException 또는 PropertyTypeNotFoundException 발생.
     */
    public void validate(RegisterPropertyCommand command) {
        partnerReadManager.verifyExists(command.partnerId());
        propertyTypeReadManager.verifyExists(command.propertyTypeId());
    }
}

package com.ryuqq.otatoy.application.roomtype.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;

import org.springframework.stereotype.Component;

/**
 * 객실 등록 검증 전용 Validator.
 * PropertyReadManager를 주입받아 숙소 존재 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RoomTypeRegistrationValidator {

    private final PropertyReadManager propertyReadManager;

    public RoomTypeRegistrationValidator(PropertyReadManager propertyReadManager) {
        this.propertyReadManager = propertyReadManager;
    }

    /**
     * 객실 등록 전 숙소 존재 여부를 검증한다.
     * 존재하지 않으면 PropertyNotFoundException 발생.
     */
    public void validate(RegisterRoomTypeCommand command) {
        propertyReadManager.verifyExists(command.propertyId());
    }
}

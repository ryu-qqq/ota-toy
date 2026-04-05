package com.ryuqq.otatoy.application.pricing.validator;

import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;

import org.springframework.stereotype.Component;

/**
 * RatePlan 등록 검증 전용 Validator.
 * RoomTypeReadManager를 주입받아 객실 유형 존재 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanRegistrationValidator {

    private final RoomTypeReadManager roomTypeReadManager;

    public RatePlanRegistrationValidator(RoomTypeReadManager roomTypeReadManager) {
        this.roomTypeReadManager = roomTypeReadManager;
    }

    /**
     * RatePlan 등록 전 객실 유형 존재 여부를 검증한다.
     * 존재하지 않으면 RoomTypeNotFoundException 발생 (AC-2).
     */
    public void validate(RegisterRatePlanCommand command) {
        roomTypeReadManager.verifyExists(command.roomTypeId());
    }
}

package com.ryuqq.otatoy.application.reservation.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;

import org.springframework.stereotype.Component;

/**
 * 예약 생성 Validator (APP-VAL-002).
 * ReadManager를 주입받아 검증한다. QueryPort 직접 주입 금지.
 * @Transactional 없음 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class CreateReservationValidator {

    private final PropertyReadManager propertyReadManager;
    private final RoomTypeReadManager roomTypeReadManager;

    public CreateReservationValidator(PropertyReadManager propertyReadManager,
                                       RoomTypeReadManager roomTypeReadManager) {
        this.propertyReadManager = propertyReadManager;
        this.roomTypeReadManager = roomTypeReadManager;
    }

    /**
     * 예약 생성에 필요한 선행 조건을 검증한다.
     * - 숙소 존재 여부
     * - 객실 유형 존재 여부
     */
    public void validate(CreateReservationCommand command) {
        propertyReadManager.verifyExists(command.propertyId());
        roomTypeReadManager.verifyExists(command.roomTypeId());
    }
}

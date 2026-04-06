package com.ryuqq.otatoy.application.reservation.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;

import org.springframework.stereotype.Component;

/**
 * 예약 세션 생성 Validator.
 * ReadManager를 주입받아 검증한다. QueryPort 직접 주입 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionValidator {

    private final PropertyReadManager propertyReadManager;
    private final RoomTypeReadManager roomTypeReadManager;

    public ReservationSessionValidator(PropertyReadManager propertyReadManager,
                                        RoomTypeReadManager roomTypeReadManager) {
        this.propertyReadManager = propertyReadManager;
        this.roomTypeReadManager = roomTypeReadManager;
    }

    /**
     * 예약 세션 생성에 필요한 선행 조건을 검증한다.
     * - 숙소 존재 여부
     * - 객실 유형 존재 여부
     */
    public void validate(CreateReservationSessionCommand command) {
        propertyReadManager.verifyExists(command.propertyId());
        roomTypeReadManager.verifyExists(command.roomTypeId());
    }
}

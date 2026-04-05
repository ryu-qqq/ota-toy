package com.ryuqq.otatoy.application.roomtype.service;

import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.application.roomtype.facade.RoomTypePersistenceFacade;
import com.ryuqq.otatoy.application.roomtype.factory.RoomTypeFactory;
import com.ryuqq.otatoy.application.roomtype.port.in.RegisterRoomTypeUseCase;
import com.ryuqq.otatoy.application.roomtype.validator.RoomTypeRegistrationValidator;

import org.springframework.stereotype.Service;

/**
 * 객실 유형 등록 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 PersistenceFacade에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class RegisterRoomTypeService implements RegisterRoomTypeUseCase {

    private final RoomTypeRegistrationValidator validator;
    private final RoomTypeFactory roomTypeFactory;
    private final RoomTypePersistenceFacade roomTypePersistenceFacade;

    public RegisterRoomTypeService(RoomTypeRegistrationValidator validator,
                                    RoomTypeFactory roomTypeFactory,
                                    RoomTypePersistenceFacade roomTypePersistenceFacade) {
        this.validator = validator;
        this.roomTypeFactory = roomTypeFactory;
        this.roomTypePersistenceFacade = roomTypePersistenceFacade;
    }

    @Override
    public Long execute(RegisterRoomTypeCommand command) {
        // 1. 검증 (Validator -- PropertyReadManager.verifyExists 경유)
        validator.validate(command);

        // 2. 번들 생성 (Factory -- RoomType + Bed + View, roomTypeId는 아직 null)
        RoomTypeBundle bundle = roomTypeFactory.createBundle(command);

        // 3. 원자적 저장 (Facade -- RoomType 저장 → ID 할당 → withRoomTypeId로 Bed/View에 ID 부여)
        return roomTypePersistenceFacade.persist(bundle);
    }
}

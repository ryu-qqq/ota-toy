package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyCommandManager;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.application.property.validator.PropertyRegistrationValidator;
import com.ryuqq.otatoy.domain.property.Property;

import org.springframework.stereotype.Service;

/**
 * 숙소 기본정보 등록 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 Manager에서 관리한다.
 * Port 직접 호출 금지 -- Manager/Factory/Validator만 의존한다 (APP-BC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class RegisterPropertyService implements RegisterPropertyUseCase {

    private final PropertyRegistrationValidator validator;
    private final PropertyFactory propertyFactory;
    private final PropertyCommandManager propertyCommandManager;

    public RegisterPropertyService(PropertyRegistrationValidator validator,
                                    PropertyFactory propertyFactory,
                                    PropertyCommandManager propertyCommandManager) {
        this.validator = validator;
        this.propertyFactory = propertyFactory;
        this.propertyCommandManager = propertyCommandManager;
    }

    @Override
    public Long execute(RegisterPropertyCommand command) {
        // 1. 검증 (Validator -- ReadManager.verifyExists 경유)
        validator.validate(command);

        // 2. 도메인 객체 생성 (Factory -- TimeProvider)
        Property property = propertyFactory.createProperty(command);

        // 3. 저장 (CommandManager -- @Transactional 메서드 단위)
        return propertyCommandManager.persist(property);
    }
}

package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PropertyPhoto 저장 트랜잭션 경계 관리자.
 * 쓰기 전용. 읽기는 PropertyPhotoReadManager가 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyPhotoCommandManager {

    private final PropertyPhotoCommandPort propertyPhotoCommandPort;

    public PropertyPhotoCommandManager(PropertyPhotoCommandPort propertyPhotoCommandPort) {
        this.propertyPhotoCommandPort = propertyPhotoCommandPort;
    }

    @Transactional
    public int persistAll(List<PropertyPhoto> photos) {
        return propertyPhotoCommandPort.persistAll(photos);
    }
}

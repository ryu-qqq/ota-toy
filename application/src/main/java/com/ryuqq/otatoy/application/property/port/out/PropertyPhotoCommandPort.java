package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyPhoto;

import java.util.List;

/**
 * PropertyPhoto 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyPhotoCommandPort {

    /**
     * 사진 목록을 일괄 저장한다.
     *
     * @param photos 저장할 사진 목록
     * @return 저장된 건수
     */
    int persistAll(List<PropertyPhoto> photos);
}

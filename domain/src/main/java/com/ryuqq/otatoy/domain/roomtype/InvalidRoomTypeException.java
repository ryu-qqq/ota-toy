package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;
import com.ryuqq.otatoy.domain.common.DomainException;

import java.util.Map;

/**
 * 객실 유형 정보가 유효하지 않을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class InvalidRoomTypeException extends DomainException {

    private final String detail;

    public InvalidRoomTypeException(String detail) {
        super(AccommodationErrorCode.INVALID_ROOM_TYPE, Map.of("detail", detail));
        this.detail = detail;
    }

    @Override
    public String getMessage() {
        return detail;
    }

    public String detail() {
        return detail;
    }
}

package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeId(Long value) {

    public PropertyTypeId {
        if (value == null) {
            throw new IllegalArgumentException("숙소 유형 ID는 필수입니다");
        }
    }

    public static PropertyTypeId of(Long value) {
        return new PropertyTypeId(value);
    }
}

package com.ryuqq.otatoy.domain.propertytype;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum PropertyTypeErrorCode implements ErrorCode {

    PROPERTY_TYPE_NOT_FOUND("PT-001", "숙소 유형을 찾을 수 없습니다", ErrorCategory.NOT_FOUND);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    PropertyTypeErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public ErrorCategory getCategory() { return category; }
}

package com.ryuqq.otatoy.domain.partner;

public record PartnerId(Long value) {

    public static PartnerId of(Long value) {
        return new PartnerId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

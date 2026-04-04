package com.ryuqq.otatoy.domain.partner;

public record PartnerId(Long value) {

    public static PartnerId of(Long value) {
        return new PartnerId(value);
    }

    public static PartnerId forNew() { return new PartnerId(null); }

    public boolean isNew() {
        return value == null;
    }
}

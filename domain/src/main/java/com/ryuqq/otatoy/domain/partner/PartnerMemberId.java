package com.ryuqq.otatoy.domain.partner;

public record PartnerMemberId(Long value) {

    public static PartnerMemberId of(Long value) {
        return new PartnerMemberId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}

package com.ryuqq.otatoy.domain.partner;

public record PartnerName(String value) {

    public PartnerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("파트너명은 필수입니다");
        }
    }

    public static PartnerName of(String value) {
        return new PartnerName(value);
    }
}

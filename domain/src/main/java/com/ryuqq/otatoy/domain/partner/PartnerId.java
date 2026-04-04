package com.ryuqq.otatoy.domain.partner;

/**
 * 파트너 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record PartnerId(Long value) {

    public static PartnerId of(Long value) {
        return new PartnerId(value);
    }

    public static PartnerId forNew() { return new PartnerId(null); }

    public boolean isNew() {
        return value == null;
    }
}
